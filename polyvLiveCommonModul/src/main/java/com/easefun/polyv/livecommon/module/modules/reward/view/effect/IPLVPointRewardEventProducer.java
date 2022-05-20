package com.easefun.polyv.livecommon.module.modules.reward.view.effect;

import com.plv.socket.event.chat.PLVRewardEvent;

/**
 * date: 2019-12-05
 * author: hwj
 * description: 积分打赏事件生产者。从socket处接受积分打赏event，并需要维护一个队列，让消费者去取这个事件。
 * 消费者通过{@link #fetchEvent(IPLVOnFetchRewardEventListener)}方法去取出事件，在回调中拿到积分打赏事件。
 * 只要调用了prepare()，那么可以在任何时候调用fetchEvent()方法，该方法是异步方法，会立刻返回。而对应的事件则等到
 * 内部维护的事件队列不为空的时候，会借由fetchEvent传入的监听器返回。
 * <p>
 * <p>
 * <p>
 * 有如下3中实现方案：
 * 1. 方案一：
 * 维护一个：OperationQueue，开启一个线程，线程循环去读取OperationQueue，若为空,阻塞，不为空，则获取一个fetch operation并获取一个eventQueue中的对象，然后回调给动画层去做动画，做完动画就又
 * 调用fetchEvent，往OperationQueue中加一个对象。
 * <p>
 * 需要：一个线程，一个队列，sychronized。
 * <p>
 * 2. 方案二：
 * 用一个HanderThread，fetchEvent()的时候，往HandlerThread中post一个操作，然后去获取eventQueue中的event，这里需要锁（不需要公平锁，因为fetchEvent操作限制在单线程中，HandlerThread）。
 * 需要：HandlerThread, ReentrentLock
 * <p>
 * 3. 方案三：
 * 每次调用fetchQueue的时候都在新的线程中（例如线程池）去读取eventQueue，并加上公平锁。
 * <p>
 * 需要：多少个fetch，就多少个阻塞的线程。公平锁。
 * <p>
 * 这里采用了方案二的实现：{@link PLVPointRewardEffectQueue}
 */
public interface IPLVPointRewardEventProducer {
    /**
     * 添加积分打赏事件，由socket接收到事件并放进来。
     */
    void addEvent(PLVRewardEvent rewardEvent);

    /**
     * 获取积分打赏事件。
     * <p>
     * 该操作是异步操作，实现类应该立刻返回，因为调用该方法的时候，内存中不一定保存着
     * 积分打赏事件，要等接收到对应的socket消息。
     */
    void fetchEvent(IPLVOnFetchRewardEventListener onFetchRewardEventListener);

    /**
     * 准备
     * 当OnPreparedListener调用后，才能进行调用{@link #fetchEvent(IPLVOnFetchRewardEventListener)}
     */
    void prepare(OnPreparedListener onPreparedListener);

    /**
     * 销毁
     */
    void destroy();

    /**
     * 生产者准备完成监听器
     */
    interface OnPreparedListener {
        void onPrepared();
    }

    /**
     * 获取积分打赏事件的监听器
     */
    interface IPLVOnFetchRewardEventListener {
        void onFetchSucceed(PLVRewardEvent event);
    }
}
