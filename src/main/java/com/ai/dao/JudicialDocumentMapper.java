package com.ai.dao;

import com.ai.pojo.JudicialDocument;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author tq
 * @date 2019/12/9 16:03
 */
@Mapper
public interface JudicialDocumentMapper {

    @Select("select doc_id,content from judicial_document where substr(create_time,1,10)=CURDATE() ")
    public List<JudicialDocument> getContent();
}
