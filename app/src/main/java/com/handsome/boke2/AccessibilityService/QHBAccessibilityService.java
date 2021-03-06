package com.handsome.boke2.AccessibilityService;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.handsome.boke2.R;

import java.util.ArrayList;
import java.util.List;

/**
 * =====作者=====
 * 许英俊
 * =====时间=====
 * 2016/11/19.
 */
public class QHBAccessibilityService extends AccessibilityService {

    private List<AccessibilityNodeInfo> parents;
    private SoundPool soundPool;

    /**
     * 当启动服务的时候就会被调用
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        parents = new ArrayList<>();
        soundPool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(this, R.raw.luckymoney_sound, 1);
    }

    private void findAndPerformAction(String text) {
// 查找当前窗口中包含“安装”文字的按钮
        if (getRootInActiveWindow() == null)
            return;
//通过文字找到当前的节点
        List<AccessibilityNodeInfo> nodes = getRootInActiveWindow().findAccessibilityNodeInfosByText(text);
        for (int i = 0; i < nodes.size(); i++) {
            AccessibilityNodeInfo node = nodes.get(i);
// 执行按钮点击行为

            if (node.getClassName().equals("android.view.View") && node.isEnabled()) {
                Log.e("node:", node.getClassName().toString());
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    /**
     * 监听窗口变化的回调
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
//        Log.e("eventType=",eventType+"");
        switch (eventType) {
            //当通知栏发生改变时
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        if (content.contains("[微信红包]")) {
                            //模拟打开通知栏消息，即打开微信
                            if (event.getParcelableData() != null &&
                                    event.getParcelableData() instanceof Notification) {
                                Notification notification = (Notification) event.getParcelableData();
                                PendingIntent pendingIntent = notification.contentIntent;
                                try {
                                    pendingIntent.send();
                                    soundPool.play(1, 1, 1, 0, 0, 1);
                                    Log.e("demo", "进入微信");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                getLastPacket();
                break;
            //当窗口的状态发生改变时
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                Log.e("className", className);
                switch (className) {
                    case "com.tencent.mm.ui.LauncherUI":
                        //点击最后一个红包
//                        Log.e("demo", "点击红包");
                        getLastPacket();
                        break;
                    case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI":
                        //开红包
//                        Log.e("demo", "开红包");
//                            inputClick("com.tencent.mm:id/bdh");
                            inputClick("com.tencent.mm:id/c4j");
//                        inputClick("com.tencent.mm:id/bi3");
                        break;
                    case "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI":
                        //退出红包
//                        Log.e("demo", "退出红包");
//                        inputClick("com.tencent.mm:id/gp");
                        inputClick("com.tencent.mm:id/hx");
                        break;

                }
                break;
        }
    }

    /**
     * 通过ID获取控件，并进行模拟点击
     *
     * @param clickId
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void inputClick(String clickId) {
//        SystemClock.sleep(400L);
//        List<AccessibilityWindowInfo> accessibilityWindowInfos=getWindows();
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(clickId);
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } else {
            Log.w("OpenFailed", "红包打开失败");
        }
    }

    /**
     * 通过ID获取控件，并进行模拟点击
     *
     * @param clickId
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void inputClick2(String clickId, AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(clickId);
            for (AccessibilityNodeInfo item : list) {
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        } else {
            Log.w("OpenFailed", "红包打开失败");
        }
    }

    /**
     * 获取List中最后一个红包，并进行模拟点击
     */
    private void getLastPacket() {
        parents=new ArrayList<>();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        recycle(rootNode);
        if (parents.size() > 0) {
            parents.get(parents.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    private void getFirstPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        recycleList(rootNode);
        if (parents.size() > 0) {
            parents.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 回归函数遍历每一个节点，并将含有"领取红包"存进List中
     *
     * @param info
     */
    public void recycle(AccessibilityNodeInfo info) {
        if (info!=null&&info.getChildCount() == 0) {
            if (info.getText() != null) {
                if ("领取红包".equals(info.getText().toString())) {
                    if (info.isClickable()) {
//                        parents.add(info.getParent());
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parents.add(parent);
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
            }
        } else {
            if(info!=null) {
                for (int i = 0; i < info.getChildCount(); i++) {
                    if (info.getChild(i) != null) {
                        recycle(info.getChild(i));
                    }
                }
            }
        }
    }

    public void recycleList(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            if (info.getText() != null) {
                if ("苏丹".equals(info.getText().toString())) {
                    if (info.isClickable()) {
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            parents.add(parent);
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    /**
     * 中断服务的回调
     */
    @Override
    public void onInterrupt() {

    }
}
