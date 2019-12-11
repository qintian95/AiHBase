package com.ai.service;

import com.ai.dao.JudicialDocumentMapper;
import com.ai.pojo.JudicialDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tq
 * @date 2019/12/9 16:10
 */
@Service
public class JudicialDocumentImpl implements JudicialDocumentService {
    @Autowired
    JudicialDocumentMapper judicialDocumentMapper;

    @Override
    public List<JudicialDocument> getContent() {
        return judicialDocumentMapper.getContent();
    }
}
