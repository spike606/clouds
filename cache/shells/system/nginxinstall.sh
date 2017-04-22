echo "------- shell nginx install and run-------"
apt-get install nginx -y
rm /etc/nginx/nginx.conf
cp /home/vagrant/nginx.conf /etc/nginx
service nginx restart
