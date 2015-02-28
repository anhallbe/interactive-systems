#!

SCRIPT_HOME=$(dirname $0)
./r1_httpd.sh &
sleep 2
./r2_rmid.sh &
sleep 2
./r3_reggie.sh &
