### 网络准备
1. 如果预先安装服务器端，和盒子端，网段最好设置在192.168.0 网段。

### 文件准备(u盘1)
1. CentOS-7-x86-64-Minimal-1511.iso
1. windows7x64.iso
1. oVirt-toolsSetup_4.1-3.fc24.iso
1. ubuntu-16.10-destop-amd64.iso
1. ubuntu-16.10-server-amd64.iso
1. vdi-agent.iso
1. virtio-win-0.1.132.iso
1. g4l-v0.53.iso
1. rufus-2.12.exe
1. ovirt-engine-appliance-4.1-20170201.1.el7.centos.noarch.rpm(现在最新版为20170406，地址为[http://resources.ovirt.org/](http://resources.ovirt.org/pub/ovirt-4.1/rpm/el7/noarch/))
1. office2016安装文件，和激活工具。
1. teamviewer_i386.deb
1. zvirt-box和zvirt-box-ui均在网上可以找到最新版本
1. windows7激活工具

### 工具准备
1. CentOS 7 u盘安装盘（u盘2）
1. ubuntu-16.10-server-amd64 u盘安装盘（u盘3）
1. g4l u盘引导盘（u盘4）

### 安装（单台服务器）
1. 以标准分区安装centOS
1. 在安装界面做好各个硬盘的mount操作
1. 安装好后，设置bond0(mode值等于4)，DNS，等等
1. 安装ovirt及其hosted-engine 
1. 添加h264 服务器优化
1. 设置NAT
1. 添加spiceoptions选项
1. 安装一台ubuntu 16.10 destop amd64的虚拟机，并在上面安装box-ui，并设置box-ui随系统自动启动
1. 设置box-ui 的unbuntu destop系统为autostart 或是 prestart状态
1. 安装teamviewer 设置开机自动打开，并设置无人值守
1. centos setenforce 0 autostart


### （两台服务器以上的）安装
1. 系统安装，mount，bond0设置都和单台一样
1. 在其中一台机器上安装ovirt，及hosted-engine，ubuntu desktop amd 64，teamviewer，box-ui(及其自动启动)，
1. 把其他的机器，以新建集群的形式，把各个主机添加进各自的集群，再添加进oVirt engine
1. 设置gluster 存储。建议4台以上的服务器做gluster。至少以stripe 2 replica 2的模式进行配置。oVirt要求必需得有replica模式。而stripe模式是为了提升存储性能。


