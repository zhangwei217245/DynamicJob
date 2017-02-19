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

sudo add-apt-repository ppa:webupd8team/java

sudo apt-get -y update

sudo apt-get -y install build-essential oracle-java8-installer docker.io python3-pip ruby git python-setuptools python-dev python3-dev python3-setuptools cifs-utils sysstat openssh-server vim emacs fish redis-server redis-tools redis-sentinel samba nautilus-share gdal-bin libgdal-dev libgdal-java

sudo systemctl restart ssh

ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Linuxbrew/linuxbrew/go/install)"

cp ~/.bashrc ~/.bashrc.bak

echo 'export PATH="$HOME/.linuxbrew/bin:$PATH"' >> ~/.bashrc
echo 'export MANPATH="$HOME/.linuxbrew/share/man:$MANPATH"' >> ~/.bashrc
echo 'export INFOPATH="$HOME/.linuxbrew/share/info:$INFOPATH"' >> ~/.bashrc

source ~/.bashrc
#echo 'fish' >> ~/.bashrc

sudo apt-get -y install linuxbrew-wrapper

brew install node redis gdal

sudo groupadd samba
sudo adduser wesley samba

sudo adduser guofeng
sudo adduser guofeng sudo

mkdir -p /home/wesley/smbdir
sudo mount -t cifs //cgstdb/Wei /home/wesley/smbdir -o username=zhang56,workgroup=TTU


chsh -s /usr/bin/fish 

sudo chown -R wesley:wesley ~/.config/fish

echo '# For homebrew' >> ~/.config/fish/config.fish
echo 'set -gx PATH "$HOME/.linuxbrew/bin" $PATH' >> ~/.config/fish/config.fish
echo 'set -gx MANPATH "$HOME/.linuxbrew/share/man" $MANPATH' >> ~/.config/fish/config.fish
echo 'set -gx INFOPATH "$HOME/.linuxbrew/share/info" $INFOPATH' >> ~/.config/fish/config.fish

echo "remember to execute : omf install cbjohnson"
curl -L https://github.com/oh-my-fish/oh-my-fish/raw/master/bin/install | fish
