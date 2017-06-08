@Echo off
echo ================ Development ================
echo Copy C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\dynamic\plugins
xcopy /y C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\dynamic\plugins
echo ================ Bungeecord =================
echo Copy C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\a\plugins
xcopy /y C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\a\plugins
echo Copy C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\b\plugins
xcopy /y C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\b\plugins
echo ================ Devnet =====================
echo Copy C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Desktop\devnet\wormholes-lobby-11\plugins
xcopy /y C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Desktop\devnet\wormholes-lobby-11\plugins
echo Copy C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Desktop\devnet\wormholes-auxiliary-11\plugins
xcopy /y C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Desktop\devnet\wormholes-auxiliary-11\plugins
echo ================ Release ====================
echo F|xcopy /y /s /f /q C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar "%1"