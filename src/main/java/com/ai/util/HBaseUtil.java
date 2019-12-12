package com.ai.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author tq
 * @date 2019/12/9 17:05
 */

@Service
public class HBaseUtil {


    private static Connection getConnection() {

        Connection connection = null;
        try {
            Configuration configuration = HBaseConfiguration.create();
            configuration.set("hbase.zookeeper.quorum", "mf04:2181,mf05:2181,mf06:2181");
            connection = ConnectionFactory.createConnection(configuration);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

    public static Table getTable(String tableName) throws IOException {
        return getConnection().getTable(TableName.valueOf(tableName));
    }

    //创建表，如果存在则不创建
    public static boolean createTable(String tableName, String cfs) {
        try (HBaseAdmin admin = (HBaseAdmin) getConnection().getAdmin()) {
            if (admin.tableExists(TableName.valueOf(tableName))) {
                return false;
            }
            HTableDescriptor tableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));

            HColumnDescriptor columnDescriptor = new HColumnDescriptor(cfs);
            columnDescriptor.setMaxVersions(1);
            tableDescriptor.addFamily(columnDescriptor);

            admin.createTable(tableDescriptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //单行插入数据
    public static void putRow(String tableName, String rowkey, String cfName, String qualifer, String data) {
        try (Table table = getTable(tableName)) {
            Put put = new Put(Bytes.toBytes(rowkey));
            put.addColumn(Bytes.toBytes(cfName), Bytes.toBytes(qualifer), Bytes.toBytes(data));
            table.put(put);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //批量插入数据
    public static void putRows(String tableName, List<Put> puts) {
        try (Table table = getTable(tableName)) {
            table.put(puts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //查询单条数据
    public static Result getRow(String tableName, String rowkey) throws IOException {
        Table table = getTable(tableName);
        Get get = new Get(Bytes.toBytes(rowkey));
        return table.get(get);

    }

    //检索数据
    public ResultScanner getScanner(String tableName, String startKey, String stopKey) throws IOException {
        Table table = getTable(tableName);
        Scan scan = new Scan();
        scan.setStartRow(Bytes.toBytes(startKey));
        scan.setStopRow(Bytes.toBytes(stopKey));
        scan.setCaching(1000);
        ResultScanner results = table.getScanner(scan);

        return results;
    }

    //检索数据加上过滤
    public static ResultScanner getScannerFilter(String tableName, String startKey, String stopKey, FilterList filterList) {
        try (Table table = getTable(tableName)) {
            Scan scan = new Scan();
            scan.setFilter(filterList);
            scan.setStartRow(Bytes.toBytes(startKey));
            scan.setStopRow(Bytes.toBytes(stopKey));
            scan.setCaching(1000);
            ResultScanner results = table.getScanner(scan);
            results.forEach(result -> {
                /**
                 * todo...
                 */
            });
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
