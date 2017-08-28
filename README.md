# godaddy-ddns
java version of ddns using godaddy api


Using this is quite simple.

This is a single file java file, compile using java 1.8, should works with >1.6(not tested). 
No additional third-party lib needed.

Usage:
#1. edit godaddy_ddns.prop, change the domain name/ key/ secret for your own web site

#2. java -jar Godaddy_ddns.jar 

It will update the ip address of your server every 10 seconds.
Once it detect the ip address changed, will update the dns record with the new ip address.

GoDaddy customers can obtain values for the KEY and SECRET arguments by creating a production key/secret at https://developer.godaddy.com/keys/.


Enjoy it.

The original from https://github.com/CarlEdman/godaddy-ddns as my server can't run this python3, so change to java version.

