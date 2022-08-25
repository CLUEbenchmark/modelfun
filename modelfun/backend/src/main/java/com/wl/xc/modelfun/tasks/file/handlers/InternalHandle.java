package com.wl.xc.modelfun.tasks.file.handlers;

import com.wl.xc.modelfun.tasks.file.FileTask;

/**
 * 为了把事务范围缩小，所以单独分了一个内部处理类出来
 * <p>
 * 但是又不想改动原有代码，所以偷懒，在现有代码里加了一个内部类，可以尽量减少代码改动。
 * <p>
 * 这样就造成了一个循环依赖的问题，虽然spring能解决，但是不推荐这么做，如果时间充足，最好重构一下代码。
 *
 * @version 1.0
 * @date 2022/5/25 9:58
 */
public interface InternalHandle {

  void internalHandle(FileTask fileTask);
}
