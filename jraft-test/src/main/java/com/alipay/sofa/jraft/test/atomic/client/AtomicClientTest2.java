package com.alipay.sofa.jraft.test.atomic.client;

import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.Status;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeoutException;

/**
 * @author: bin.liu
 * @date 2020/6/18 16:51
 */
public class AtomicClientTest2 {

    public static void main(String[] args) throws TimeoutException, InterruptedException, BrokenBarrierException {

        final RouteTable table = RouteTable.getInstance();
        table.updateConfiguration("atomic",
                JRaftUtils.getConfiguration("127.0.0.1:8609,127.0.0.1:8610,127.0.0.1:8611"));
        final CliClientServiceImpl cliClientService = new CliClientServiceImpl();
        cliClientService.init(new CliOptions());
        final Status st = table.refreshLeader(cliClientService, "atomic", 10000);
        System.out.println(st);

        final AtomicClient cli = new AtomicClient("atomic", JRaftUtils.getConfiguration("localhost:8610"));
        final PeerId leader = table.selectLeader("atomic");
        cli.start();

        final int threads = 30;
        final int count = 10000;
        final CyclicBarrier barrier = new CyclicBarrier(threads + 1);

        for (int t = 0; t < threads; t++) {
            new Thread() {
                @Override
                public void run() {
                    long sum = 0;
                    try {
                        barrier.await();
                        for (int i = 0; i < count; i++) {
//                            sum += cli.get(leader, "a", true, false);
                            sum += cli.addAndGet(leader, "a", i);
                        }
                        barrier.await();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("sum=" + sum);
                    }
                }
            }.start();

        }
        final long start = System.currentTimeMillis();
        barrier.await();
        final long cost = System.currentTimeMillis() - start;
        final long tps = Math.round(threads * count * 1000.0 / cost);
        System.out.println("tps=" + tps + ",cost=" + cost + " ms.");
        cli.shutdown();

//        final Configuration conf = new Configuration();
//        if (!conf.parse(confStr)) {
//            throw new IllegalArgumentException("Fail to parse conf:" + confStr);
//        }
//
//        RouteTable.getInstance().updateConfiguration(groupId, conf);
//
//        final CliClientServiceImpl cliClientService = new CliClientServiceImpl();
//        cliClientService.init(new CliOptions());
//
//        if (!RouteTable.getInstance().refreshLeader(cliClientService, groupId, 1000).isOk()) {
//            throw new IllegalStateException("Refresh leader failed");
//        }

//        final PeerId leader = RouteTable.getInstance().selectLeader(groupId);
//        System.out.println("Leader is " + leader);
//        final int n = 10000;
//        final CountDownLatch latch = new CountDownLatch(n);
//        final long start = System.currentTimeMillis();
//        for (int i = 0; i < n; i++) {
////            incrementAndGet(cliClientService, leader, i, latch);
//        }
//        latch.await();
//        System.out.println(n + " ops, cost : " + (System.currentTimeMillis() - start) + " ms.");
//        System.exit(0);

    }
}
