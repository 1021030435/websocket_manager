// package org.jeecg.websocket.task;
//
/// **
// * @author: Zz Ai
// * @date: 2021-08-05 14:07
// **/
//
// import java.util.concurrent.ExecutionException;
// import java.util.concurrent.SynchronousQueue;
// import java.util.concurrent.ThreadPoolExecutor;
// import java.util.concurrent.TimeUnit;
//
/// **
// * @program: jeecg-boot-parent
// *
// * @description:
// *
// * @author: Zz Ai
// *
// * @create: 2021-08-05 14:07
// **/
// public class TaskLoop {
//
// public static void main(String[] args) throws ExecutionException, InterruptedException {
// SynchronousQueue<Runnable> workQueue = new SynchronousQueue<>();
//
// ThreadPoolExecutor influxThreadPool =
// new ThreadPoolExecutor(4, 16, 3000, TimeUnit.SECONDS, workQueue, new ThreadPoolExecutor.CallerRunsPolicy());
// // Future<String> submit = influxThreadPool.submit(() -> {
// // Thread.sleep(5000);
// // return "1";
// // });
// // Future<String> s2 = influxThreadPool.submit(() -> {
// // Thread.sleep(5000);
// // return "2";
// // });
// workQueue.add(() -> {
// System.err.println("asdasd");
// });
//
// // String s = submit.get();
// // String s22 = s2.get();
// // System.err.println(s);
// // System.err.println(s22);
//
// Thread.sleep(9999999);
//
// }
//
// }
