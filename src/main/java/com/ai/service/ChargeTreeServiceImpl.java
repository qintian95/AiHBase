package com.ai.service;

import com.ai.dao.ChargeTreeMapper;
import com.ai.pojo.ChargeTree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tq
 * @date 2019/12/9 15:43
 */
@Service
public class ChargeTreeServiceImpl implements ChargeTreeService {
    @Autowired
    ChargeTreeMapper mapper;
    @Override
    public List<ChargeTree> getChargeTree() {
        return mapper.getChargeTree();
    }
}
