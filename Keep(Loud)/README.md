### 应用说明
让联想小新Pad Pro 12.7保持四扬声器播放声音。
- 使用扬声器和录音功能一起工作的原理，强开四扬
- 应用需在后台保持运行
- 完全开源，完全免费，无需捐赠，欢迎参与贡献，欢迎提issues，谢绝盗用或用于违法行为
- 完整代码来自 @aimmarc
- @aimmarc 的主页 https://github.com/aimmarc/
- @ZxsRegards 的主页https://github.com/ZxsRegards/

- keep在后台的运行在目前并不是很稳定
-我针对 @aimmarc 的录音核心代码,重新设计了下Keep,我给Keep添加了自启动，磁贴，开机广播，前台通知保活，无障碍，悬浮窗，快捷方式。
-目前在 酷安@兰微卡鱼 开的ColorOS15包中实现完美开机自启,后台保活。HyperOS虽然没刷,但是依然适配 图3 图4 对HyperOS自启动代码,功能应该都正常。
-可通过控制中心开启或通过启动TransparentStarterActivity活动来开启录音。
-固化为系统应用在/system/product/app/目录下,后台清理根本不会掉,目前可长时间常驻后台,对媒体音量判断为0时,不采取录音。

><i class="iconfont icon-gengduo" style="font-size: 22px"></i   