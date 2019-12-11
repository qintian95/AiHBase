package com.ai.service;

import com.ai.pojo.ChargeTree;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tq
 * @date 2019/12/9 15:41
 */

public interface ChargeTreeService {
    List<ChargeTree> getChargeTree();
}
