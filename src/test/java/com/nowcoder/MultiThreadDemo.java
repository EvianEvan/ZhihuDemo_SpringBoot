package com.nowcoder;

// 视频 5：多线程介绍：
// 本项目中并没有实际使用多线程，只是在在测试包下做了一个Demo；
// 内容很简单，只是内容较多；

//
//
// part1: 介绍自定义线程的方法：

// 自定义一个线程 有两种方法:
// 方法 1. 通过 继承 Thread:        extends Thread，重载run()方法
// 方法 2. 通过 实现 Runnable接口： implements Runnable()，实现run()方法

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// 自定义一个线程 方法 1：
class MyThread extends Thread {
  // 定义进程的 id；
  private int tid;

  public MyThread(int tid) {
    this.tid = tid;
  }

  // 重写 run方法：每个线程都打印 10个变量：
  @Override
  public void run() {
    try {
      for (int i = 0; i < 10; i++) {
        Thread.sleep(1000);
        System.out.println(String.format("创建线程方法1：线程ID：%d；变量i：%d", tid, i));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

public class MultiThreadDemo {

  // 测试函数 ：测试自定义线程：
  public static void testMyThread() {
    // 测试自定义一个线程 方法 1：同时启动十个线程：
    //    for (int i = 0; i < 10; i++) {
    //      new MyThread(i).start();
    //    }

    // 测试自定义一个线程 方法 2： 通过 实现 Runnable接口： implements Runnable()，实现run()方法:
    // 同时启动十个线程：
    for (int i = 0; i < 10; i++) {
      // 该变量必须定义为 final，否则出错：
      // 局部内部类(Thread)（包括匿名局部内部类和普通内部类）中使用局部变量，那么这个局部变量必须使用final修饰。
      // Error:(50, 82) java:
      // local variables referenced from an inner class must be final or effectively final；
      final int finalI = i;
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    for (int j = 0; j < 10; j++) {
                      Thread.sleep(1000);
                      System.out.println(String.format("创建线程方法2：线程ID：%d；变量j：%d", finalI, j));
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                }
              })
          .start();
    }
  }

  //
  //
  // part2: 介绍 synchronized锁：
  // 1. 放在方法上会锁住所有synchronized方法;
  // 2. synchronized(obj) 锁住相关的代码段;
  //
  // 定义一个用来锁的对象：
  private static final Object obj = new Object();

  // 测试函数 ：测试锁：
  // 定义打印函数 1：
  public static void testSynchronized1() {
    // 需要取得锁的对象；
    synchronized (obj) {
      try {
        for (int j = 0; j < 10; j++) {
          Thread.sleep(100);
          System.out.println(String.format("测试锁1：变量j：%d", j));
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // 定义打印函数 2：
  public static void testSynchronized2() {
    // 需要取得锁的对象；
    synchronized (obj) {
      // 测试让线程放弃锁的争抢：
      // synchronized (new Object()) {
      try {
        for (int j = 0; j < 10; j++) {
          Thread.sleep(100);
          System.out.println(String.format("测试锁2：变量j：%d", j));
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static void testSynchronized() {
    // 同时启动 10个线程：
    // 两个打印函数 testSynchronized1 和 testSynchronized2 都需要同一个对象的锁，
    // 所以两个函数不会同时执行，同时只能执行一个函数；
    for (int i = 0; i < 10; i++) {
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  testSynchronized1();
                  testSynchronized2();
                }
              })
          .start();
    }
  }

  //
  //
  // part3: 介绍阻塞队列 BlockingQueue: 实现异步：一边在存一边在取；
  // 后面课程中介绍的 Redis也实现了类似的异步处理思想：（只是一个从BlockingQueue中取，一个从Redis中取）
  // 类似于线程池：
  // 1.定义消费者线程（类）： Consumer 一直从 BlockingQueue中取：
  static class Consumer implements Runnable {
    private BlockingQueue<String> q;

    public Consumer(BlockingQueue<String> q) {
      this.q = q;
    }

    @Override
    public void run() {
      try {
        while (true) {
          System.out.println(Thread.currentThread().getName() + ":" + q.take());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // 2.定义生产者线程（类）： Producer 一直往 BlockingQueue中存：
  static class Producer implements Runnable {
    private BlockingQueue<String> q;

    public Producer(BlockingQueue<String> q) {
      this.q = q;
    }

    @Override
    public void run() {
      try {
        for (int i = 0; i < 100; i++) {
          Thread.sleep(1000);
          q.put(String.valueOf(i));
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // 3.执行 BlockingQueue：
  public static void testBlockingQueue() {
    // 容量为10的阻塞队列：
    BlockingQueue<String> q = new ArrayBlockingQueue<String>(10);
    // 启动一个生产者线程，两个消费者线程：
    new Thread(new Producer(q)).start();
    new Thread(new Consumer(q), "Consumer1").start();
    new Thread(new Consumer(q), "Consumer2").start();
  }

  //
  //
  // part4: 介绍线程局部变量（线程独立变量） ThreadLocal: 每个线程都有一个该变量的副本；
  // 1. 线程局部变量：即使是一个static成员，每个线程访问的变量也是不同的。
  // 2. 常见于web中存储当前用户到一个静态工具类中，在线程的任何地方(任何controller/service等)都可以访问到当前线程的用户。
  // 3. 参考HostHolder.java里保存的 当前访问用户 users；（在拦截器部分中讲过）
  private static ThreadLocal<Integer> threadLocalUserIds = new ThreadLocal<>();
  private static int userId;

  public static void testThreadLocal() {
    // 同时启动十个线程：
    for (int i = 0; i < 10; i++) {
      final int finalI = i;
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    // 如不使用 ThreadLocal:
                    userId = finalI;
                    Thread.sleep(1000);
                    System.out.println("UserId: " + userId);

                    // 如使用 ThreadLocal：
                    threadLocalUserIds.set(finalI);
                    Thread.sleep(1000);
                    System.out.println("ThreadLocal: " + threadLocalUserIds.get());

                    // 测试输出：
                    // (不使用 ThreadLocal时，由于每个线程都睡了1秒，第九条线程把 i设成了9，所以全都输出最后一个 i)
                    // (使用 ThreadLocal时，由于每个线程都有一个副本，所以每个线程都有一个独立的 i)
                    // UserId: 9
                    // UserId: 9
                    // UserId: 9
                    // UserId: 9
                    // UserId: 9
                    // UserId: 9
                    // UserId: 9
                    // UserId: 9
                    // UserId: 9
                    // UserId: 9
                    // ThreadLocal: 4
                    // ThreadLocal: 3
                    // ThreadLocal: 8
                    // ThreadLocal: 2
                    // ThreadLocal: 5
                    // ThreadLocal: 6
                    // ThreadLocal: 7
                    // ThreadLocal: 0
                    // ThreadLocal: 1
                    // ThreadLocal: 9

                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                }
              })
          .start();
    }
  }

  //
  //
  // part5: 介绍线程池的概念：（线程池：为了方便管理线程）
  //
  // Executor:
  // 一个任务框架，可以把各种各样的任务提交进来，提交之后它会分配已有的线程一个一个地去执行；
  // 1. 提供一个运行任务的框架。
  // 2. 将任务和如何运行任务解耦。
  // 3. 常用于提供线程池或定时任务服务（定时任务：时间一到，线程池空闲就会去执行）。

  // 使用线程池时分两步：1.创建线程池；2.提交任务；
  // 具体讲解去看 Java官方文档；

  public static void testExecutor() {
    // 创建线程池（ExecutorService）：创建一个 单线程/多线程 的线程池：
    // ExecutorService pool = Executors.newSingleThreadExecutor();
    ExecutorService pool = Executors.newFixedThreadPool(2);
    // 提交任务(一个线程)：每个一秒打印一个数字：
    pool.submit(
        new Runnable() {
          @Override
          public void run() {
            for (int i = 0; i < 10; i++) {
              try {
                Thread.sleep(1000);
                System.out.println("Executor1: " + i);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        });
    // 提交第二个任务：
    pool.submit(
        new Runnable() {
          @Override
          public void run() {
            for (int i = 0; i < 10; i++) {
              try {
                Thread.sleep(1000);
                System.out.println("Executor2 " + i);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }
        });
    // 若创建了一个两个线程的线程池，然后由又提交了两个任务，则两个线程会各分配一个任务：
    // 输出：
    // Executor1: 0
    // Executor2 0
    // Executor1: 1
    // Executor2 1
    // Executor2 2
    // Executor1: 2
    // ..
    // Executor2 8
    // Executor1: 8
    // Executor1: 9
    // Executor2 9

    // 将 pool关闭（不是强制关闭，而是等任务执行完再关闭）：
    pool.shutdown();
    // 执行shutdown之后，在主线程中轮询地查看 pool是否已经真正结束：（等真正结束之后可以去做其他事情）
    // (主线程每隔一秒去检查一次 pool是否已经关闭)
    while (!pool.isTerminated()) {
      try {
        Thread.sleep(1000);
        System.out.println("Waiting for termimation...");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // 输出：
    // Waiting for termimation...
    // Executor1: 0
    // Executor2 0
    // Waiting for termimation...
    // Executor1: 1
    // Executor2 1
    // Waiting for termimation...
    // Executor2 2
    // Executor1: 2
    // ..
    // Waiting for termimation...
    // Executor1: 7
    // Executor2 7
    // Executor1: 8
    // Waiting for termimation...
    // Executor2 8
    // Waiting for termimation...
    // Executor2 9
    // Executor1: 9
    // Waiting for termimation...
  }

  // Future: 表示异步的结果：用于线程之间的通信（传递信息）：
  // 1. 可以异步地去 等待执行返回的结果；
  // 2. 可以阻塞地去 等待执行返回的结果（使用 get方法）；
  // 3. 等待执行返回的结果时 可以设置 等待的限制时间（timeout）；
  // 4. 也可以获取到线程中的 Exception；
  //
  // A Future represents the result of an asynchronous computation.
  //
  // An {Executor} that provides methods to manage termination and
  // methods that can produce a {Future} for tracking progress of
  // one or more asynchronous tasks.
  // 具体讲解去看 Java官方文档；
  //
  // 可以和 Executor框架结合在一起使用：
  // Executor框架在提交完任务后就不做任何事了，可以在提交完任务以后，记录 pool返回的 Future结果；
  public static void testFuture() {
    // 创建线程池：
    ExecutorService pool = Executors.newSingleThreadExecutor();
    // 提交任务，并使用一个 Future记录该任务未来会返回的结果（返回值）；
    Future<Integer> future =
        pool.submit(
            new Callable<Integer>() {
              @Override
              public Integer call() throws Exception {
                Thread.sleep(3000);
                return 1;
              }
            });
    // 让 pool等任务完成后尝试结束(此句在此函数中不会产生任何影响，只是为了完整性才写)；
    pool.shutdown();
    // 测试输出：
    try {
      // Future在等待任务完成后才会去 get结果；
      System.out.println(future.get());
      // 也可以为 get方法设置一个 timeout时间参数，若超过 timeout时间后仍未 get到结果则会报错（TimeoutException）；
      // System.out.println(future.get(100, TimeUnit.MILLISECONDS));
    } catch (Exception e) {
      e.printStackTrace();
    }
    // 输出：
    // 在等待 3秒后显示输出 1；
  }

  //
  //
  // part6: 介绍线程安全的原子操作类型的变量：
  // 在使用多线程时，应使用线程安全的变量（原子类型的变量，详见Java文档，concurren包）；
  private static int counter = 0;
  private static AtomicInteger automicCounter = new AtomicInteger(0);

  public static void testWithoutAtomic() {
    // 同时启动十条线程：
    for (int i = 0; i < 10; i++) {
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    // 每个线程等一秒后将 counter计数器加一；
                    Thread.sleep(1000);
                    for (int j = 0; j < 10; j++) {
                      counter++;
                      System.out.println(counter);
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                }
                // 易错点：不要忘记写  .start()! 否则线程不会启动！
              })
          .start();
    }
    // 输出中最大的数是98，而不是正确结果100（10个线程各加10次）；
    // 因为出现了变量的重复写入（普通类型变量的自增不是原子操作）,所以结果总会小于100；
  }

  public static void testAtomic() {
    // 同时启动十条线程：
    for (int i = 0; i < 10; i++) {
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    // 每个线程等一秒后将 counter计数器加一；
                    Thread.sleep(1000);
                    for (int j = 0; j < 10; j++) {

                      // 改为使用原子类型变量；
                      System.out.println(automicCounter.incrementAndGet());
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                }
                // 易错点：不要忘记写  .start()! 否则线程不会启动！
              })
          .start();
    }
    // 输出中最大的数必为正确结果100（10个线程各加10次）；
  }

  //
  //
  // 主体运行函数：
  public static void main(String[] args) {
    // testMyThread();
    testSynchronized();
    // testBlockingQueue();
    // testThreadLocal();
    // testExecutor();
    // testFuture();
    // testWithoutAtomic();
    // testAtomic();
  }

  // 建议去自学的拓展内容：Java官方文档：Java中的信号量、临界区、其它类型的锁；
  // 这些都是操作系统中的东西；
}
