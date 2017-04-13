#!/bin/bash

echo "------- shell gethadoopslaves -------"

scp -r vagrant@hadoop-master:/usr/local/hadoop /home/vagrant/hadoop
rm -rf /usr/local/hadoop
mv /home/vagrant/hadoop /usr/local/
chown vagrant:vagrant -R /usr/local/hadoop
