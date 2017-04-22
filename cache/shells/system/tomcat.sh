echo "------- shell tomcat -------"
apt install tomcat7 -y
rm /etc/default/tomcat7
cp /home/vagrant/tomcat7 /etc/default
