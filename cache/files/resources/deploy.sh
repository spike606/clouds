echo "------- shell deploy war -------"
rm -r /var/lib/tomcat7/webapps/ROOT
rm /var/lib/tomcat7/webapps/ROOT.war
cp /home/vagrant/ROOT.war /var/lib/tomcat7/webapps
service tomcat7 restart
