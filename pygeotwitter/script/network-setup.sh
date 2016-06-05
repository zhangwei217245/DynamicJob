#!/bin/bash

sudo apt-get -y install curl axel wget ntp ntpdate
sudo ntpdate ntp.ubuntu.com
mkdir -p ~/Develop/git
mkdir -p ~/Data

sudo ifconfig

eth=`ifconfig | grep "encap:Ethernet"|grep -v "docker" | awk -F' ' '{print $1}'`
echo "Please confirm ethernet card name: default=$eth"

read eth1

if [ "x" = "x$eth1" ]
then
	eth=$eth
else
	eth=$eth1
fi

echo "Now the ethernet card you choose is $eth ."

echo "Please input the hostname you want:"

read hostName

sudo hostnamectl set-hostname ${hostName}
sudo echo "" >> /etc/network/interfaces
sudo echo "auto ${eth}" >> /etc/network/interfaces
sudo echo "iface ${eth} inet dhcp" >> /etc/network/interfaces
sudo echo "	hostname ${hostName}" >> /etc/network/interfaces

sudo dhclient -v ${eth}
sudo dhclient -v -r ${eth}
sudo dhclient -v ${eth}

sudo ifup --force ${eth}
sudo ifdown --force ${eth}
sudo ifup --force ${eth}

