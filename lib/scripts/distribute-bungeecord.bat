@Echo off
echo ================ Batch ================
echo Copy C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\a\plugins
xcopy /y C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\a\plugins
echo Copy C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\b\plugins
xcopy /y C:\Users\cyberpwn\Documents\development\workspace\WormholesPlugin\target\Wormholes.jar C:\Users\cyberpwn\Documents\development\servers\b\plugins
echo =======================================