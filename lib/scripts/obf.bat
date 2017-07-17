@Echo off
echo ================ Development ================
echo Copy C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\dynamic\plugins
xcopy /y C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\dynamic\plugins
echo ================ Bungeecord =================
echo Copy C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\da\plugins
xcopy /y C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\da\plugins
echo Copy C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\db\plugins
xcopy /y C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\db\plugins
echo ================ Release ====================
echo F|xcopy /y /s /f /q C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar "%1"
echo ================ Obfuscate ==================
java -Xmx4G -Xms1M -jar C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\lib\scripts\lib\proguard.jar @C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\obf\obf.cfg
echo Copying Obfuscated jar to release folder
echo F|xcopy /y /s /f /q C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\obf\obfuscated.jar "%2"
echo Writing Mapped reference
echo F|xcopy /y /s /f /q C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\obf\mapping.txt "%3"
echo Cleaning Resources...
del C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\obf\obfuscated.jar
del C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\obf\mapping.txt