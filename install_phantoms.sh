#bash
cd 
yum install fontconfig freetype freetype-devel fontconfig-devel libstdc++ -y
wget https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-1.9.8-linux-x86_64.tar.bz2
tar -xjvf phantomjs-1.9.8-linux-x86_64.tar.bz2
mv phantomjs-1.9.8-linux-x86_64 /opt/phantomjs
ln -s /opt/phantomjs/bin/phantomjs /usr/bin/phantomjs
phantomjs /opt/phantomjs/examples/hello.js