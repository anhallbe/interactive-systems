#!
# 06-feb-2012/FK Update for Ubuntu Linux
# -- ----------------------------------------------------------------
# -- This file is for Unix/Linux systems.
# -- This file starts Reggie, the Jini lookup server
# -- ----------------------------------------------------------------

SCRIPT_HOME=$(dirname $0)

LABROOT=${SCRIPT_HOME}/../..

LOG=/var/tmp/reggie_log

PCY=${LABROOT}/lib/policy.all

CFG=${SCRIPT_HOME}/httpd.cfg

if [ -a $CFG ]; then
    . $CFG
    HTTP=$CODEBASE
fi

CBS=${HTTP}/reggie-dl.jar

JAR=${LABROOT}/lib/reggie.jar

echo "******************"
echo "PCY=$PCY"
echo "LOG=$LOG"
echo "CBS=$CBS"
echo "JAR=$JAR"

if [ -d $LOG ]; then
    echo "Removing $LOG"
    rm -r $LOG
fi

unset CLASSPATH

java -jar $JAR $CBS $PCY $LOG public

