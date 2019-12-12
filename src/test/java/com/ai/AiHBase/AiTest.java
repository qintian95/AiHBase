package com.ai.AiHBase;

import cn.huacloud.platform.sdk.client.AIOpenClient;
import com.ai.util.HBaseUtil;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;



import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author tq
 * @date 2019/12/9 16:50
 */
public class AiTest {
    public static void main(String[] args) throws IOException {
       // 参入需要识别的文档
        String text = "贵州省临鑫市平安县人民检察院起诉书xx公诉刑诉〔2018〕x号被告人徐某松，曾用名徐某，男，1996年9月26日出生，公民身份证号码522225xxxxxxxxxxxx，汉族，初中文化，无业，户籍所在地贵州省临鑫市平安县，住平安县西凉县三星村三星组。因涉嫌故意伤害罪于2018年2月20日被平安县公安局决定取保候审，于2018年6月1日被本院决定取保候审。被害人黄某，曾用名无，男，1993年4月21日出生，公民身份证号码522225xxxxxxxxxxxx，汉族，高中文化，务工，户籍所在地贵州省临鑫市平安县，住平安县西凉县第二十四区街道办事处105号7单元左手拇指离断伤损伤程度属重伤二级；左肘部皮肤裂伤、尺侧腕屈肌不完全断裂及左尺骨骨折损伤程度属轻微伤。本案由平安县公安局侦查终结，以被告人徐某松涉嫌故意伤害罪于2018年5月11日移送审查起诉。本院受理后，于2018年5月12日告知被告人依法享有的诉讼权利，于2018年5月12日告知被害人依法享有的诉讼权利；已依法讯问被告人，听取被害人的意见，审查了全部案件材料。经依法审查查明：2018年2月18日22时许，被告人徐某松与其妻子杨某琴及朋友徐某琴、柳某某、白某、卢某六人一起在平安县西凉县新区下街“幸福”奶茶店内一桌喝奶茶，被害人黄某与周某、杨某、简某四人在该奶茶店内另一桌喝奶茶。因黄某与杨某琴谈过恋爱，当徐某松听见黄某等人在讨论杨某琴时，并质问黄某导致二人发生争吵。随即徐某松到冯某胜家中拿了一把单刃刀藏在身上后返回奶茶店，徐某松持刀将黄某的左手拇指砍断，又继续在朝黄某身上砍了几刀，随后离开现场。经贵州省平安县人民医院法医司法鉴定中心鉴定，被害人黄某左手拇指离断伤损伤程度属重伤二级；左肘部皮肤裂伤、尺侧腕屈肌不完全断裂及左尺骨骨折损伤程度属轻微伤。2018年2月19日，被告人徐某松主动到公安机关投案后如实供述其上述犯罪事实。7.视听资料、电子数据：辨认现场视频、提取笔录视频、同步讯问视频光盘。本院认为，被告人徐某松故意伤害他人身体致其重伤，其行为已触犯了《中华人民共和国刑法》第二百三十四条第二款规定，犯罪事实清楚，证据确实充分，应当以故意伤害罪追究其刑事责任。根据《中华人民共和国刑事诉讼法》第一百七十二条的规定，提起公诉，请依法判处。此致贵州省平安县人民法院检察员：何xx2018年6月4日附：1.被告人徐某松现取保候审在家，联系电话15xxxxxxxxx。2.案卷材料和证据2册，光盘4张、刑事附带民事诉讼状1份。";

        //业务ID，可在开发者平台中NER聚合接口的参数查询页中查找
        String businessId = "xsyspj";

        // 创建客户端，这里可以变成单例。供其他业务调用平台接口
        AIOpenClient aiOpenClient =
                new AIOpenClient("http://ai.hua-cloud.com.cn:8072/api", "test", "123456");

        //调用NER聚合接口
        Map<String, Object> result = aiOpenClient.nerAggregation(businessId, text);

        //使用fastjson将结果转换成String打印到控制台
//        System.out.println(result);

//        System.out.println(JSON.toJSONString(result).replaceAll("[\\[]]","null"));

        JSONObject jsonObject=JSON.parseObject(JSON.toJSONString(result).replaceAll("[\\[]]","null"));


        JSONObject document_reference=jsonObject.getJSONObject("document_reference");
        JSONObject defendants0=jsonObject.getJSONArray("defendants").getJSONObject(0);
        JSONObject defendants1=jsonObject.getJSONArray("defendants").getJSONObject(1);
        JSONObject accusation=jsonObject.getJSONObject("accusation");
        JSONObject judgment=jsonObject.getJSONObject("judgment");
        JSONObject hear_info=jsonObject.getJSONObject("hear_info");
        JSONObject evidences=jsonObject.getJSONObject("evidences");

        String document_reference2trail=document_reference.getString("trail");
        String document_reference2JDN=document_reference.getString("JDN");
        String document_name=jsonObject.getString("document_name");

        String defendants02defendant_preConvictions2preConviction_time=defendants0.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_time");
        String defendants02defendant_preConvictions2preConviction_chargeName=defendants0.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_chargeName");
        String defendants02defendant_preConvictions2preConviction_release=defendants0.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_release");

        String defendants02charges2suspect_charge_name2index=defendants0.getJSONArray("charges").getJSONObject(0).getJSONObject("suspect_charge_name").getString("index");
        String defendants02charges2suspect_charge_name2text=defendants0.getJSONArray("charges").getJSONObject(0).getJSONObject("suspect_charge_name").getString("text");

        String defendants02charges2charge_enforcements2charge_enforcement_type2index0=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_type").getString("index");
        String defendants02charges2charge_enforcements2charge_enforcement_type2text0=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_type").getString("text");

        String defendants02charges2charge_enforcements2charge_enforcement_time2index0=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_time").getString("index");
        String defendants02charges2charge_enforcements2charge_enforcement_time2text0=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_time").getString("text");

        String defendants02charges2charge_enforcements2charge_enforcement_organ2index0=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_organ").getString("index");
        String defendants02charges2charge_enforcements2charge_enforcement_organ2text0=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_organ").getString("text");

        String defendants02charges2charge_enforcements2charge_enforcement_type2index1=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_type").getString("index");
        String defendants02charges2charge_enforcements2charge_enforcement_type2text1=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_type").getString("text");

        String defendants02charges2charge_enforcements2charge_enforcement_time2index1=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_time").getString("index");
        String defendants02charges2charge_enforcements2charge_enforcement_time2text1=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_time").getString("text");

        String defendants02charges2charge_enforcements2charge_enforcement_organ2index1=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_organ").getString("index");
        String defendants02charges2charge_enforcements2charge_enforcement_organ2text1=defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_organ").getString("text");

        String defendants02defendant_base2NN=defendants0.getJSONObject("defendant_base").getString("NN");

        String defendants02defendant_base2BD2index=defendants0.getJSONObject("defendant_base").getJSONObject("BD").getString("index");
        String defendants02defendant_base2BD2text=defendants0.getJSONObject("defendant_base").getJSONObject("BD").getString("text");

        String defendants02defendant_base2PS = defendants0.getJSONObject("defendant_base").getString("PS");
        String defendants02defendant_base2defendant_nativePlace=defendants0.getJSONObject("defendant_base").getString("defendant_nativePlace");

        String defendants02defendant_base2E2index=defendants0.getJSONObject("defendant_base").getJSONObject("E").getString("index");
        String defendants02defendant_base2E2text=defendants0.getJSONObject("defendant_base").getJSONObject("E").getString("text");

        String defendants02defendant_base2FN2index=defendants0.getJSONObject("defendant_base").getJSONObject("FN").getString("index");
        String defendants02defendant_base2FN2text=defendants0.getJSONObject("defendant_base").getJSONObject("FN").getString("text");

        String defendants02defendant_base2defendant_capacity=defendants0.getJSONObject("defendant_base").getString("defendant_capacity");

        String defendants02defendant_base2defendant_occupation2index=defendants0.getJSONObject("defendant_base").getJSONObject("defendant_occupation").getString("index");
        String defendants02defendant_base2defendant_occupation2text=defendants0.getJSONObject("defendant_base").getJSONObject("defendant_occupation").getString("text");

        String defendants02defendant_base2DR2index=defendants0.getJSONObject("defendant_base").getJSONObject("DR").getString("index");
        String defendants02defendant_base2DR2text=defendants0.getJSONObject("defendant_base").getJSONObject("DR").getString("text");

        String defendants02defendant_base2S2index=defendants0.getJSONObject("defendant_base").getJSONObject("S").getString("index");
        String defendants02defendant_base2S2text=defendants0.getJSONObject("defendant_base").getJSONObject("S").getString("text");

        String defendants02defendant_base2defendant_identity=defendants0.getJSONObject("defendant_base").getString("defendant_identity");

        String defendants02defendant_base2defendant_name2index=defendants0.getJSONObject("defendant_base").getJSONObject("defendant_name").getString("index");
        String defendants02defendant_base2defendant_name2text=defendants0.getJSONObject("defendant_base").getJSONObject("defendant_name").getString("text");

        String defendants02defendant_base2FA2index=defendants0.getJSONObject("defendant_base").getJSONObject("FA").getString("index");
        String defendants02defendant_base2FA2text=defendants0.getJSONObject("defendant_base").getJSONObject("FA").getString("text");

        String defendants02defendant_base2special_identity2identity_others=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_others");
        String defendants02defendant_base2special_identity2identity_star=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_star");
        String defendants02defendant_base2special_identity2identity_partygroupings=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_partygroupings");
        String defendants02defendant_base2special_identity2identity_intellectual=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_intellectual");
        String defendants02defendant_base2special_identity2identity_cppcc_members=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_cppcc_members");
        String defendants02defendant_base2special_identity2identity_sportstar=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_sportstar");
        String defendants02defendant_base2special_identity2identity_officer=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_officer");
        String defendants02defendant_base2special_identity2identity_npc_member=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_npc_member");
        String defendants02defendant_base2special_identity2identity_foreigner=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_foreigner");
        String defendants02defendant_base2special_identity2identity_taiwan=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_taiwan");
        String defendants02defendant_base2special_identity2identity_leader=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_leader");
        String defendants02defendant_base2special_identity2identity_hongkongmacau=defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_hongkongmacau");


        String defendants02defendant_base2PN2index=defendants0.getJSONObject("defendant_base").getJSONObject("PN").getString("index");
        String defendants02defendant_base2PN2text=defendants0.getJSONObject("defendant_base").getJSONObject("PN").getString("text");

        String defendants02defendant_base2BA=defendants0.getJSONObject("defendant_base").getString("BA");

        String defendants02judgment2penalties2penalty_notimprison=defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_notimprison");
        String defendants02judgment2penalties2penalty_principal=defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_principal");
        String defendants02judgment2penalties2penalty_suspend=defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_suspend");
        String defendants02judgment2penalties2JA=defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("JA");
        String defendants02judgment2penalties2penalty_duration=defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_duration");


        JSONObject penalty_supplementaries=defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getJSONArray("penalty_supplementaries").getJSONObject(0);
        String defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_money=penalty_supplementaries.getString("penalty_supplementary_money");
        String defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_content=penalty_supplementaries.getString("penalty_supplementary_content");
        String defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_type=penalty_supplementaries.getString("penalty_supplementary_type");

        String defendants02judgment2defendant_joint_crime=defendants0.getJSONObject("judgment").getString("defendant_joint_crime");

        JSONObject attorneys=defendants0.getJSONArray("attorneys").getJSONObject(0);
        String defendants02attorneys2attorney_type=attorneys.getString("attorney_type");
        String defendants02attorneys2attorney_workplace=attorneys.getString("attorney_workplace");
        String defendants02attorneys2attorney_relation=attorneys.getString("attorney_relation");
        String defendants02attorneys2attorney_name=attorneys.getString("attorney_name");
        String defendants02attorneys2attorney_occupation=attorneys.getString("attorney_occupation");

        String defendants02detain2inPrison=defendants0.getJSONObject("detain").getString("inPrison");
        String defendants02detain2inPrison_place=defendants0.getJSONObject("detain").getString("inPrison_place");

        String defendants12defendant_preConvictions2preConviction_time=defendants1.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_time");
        String defendants12defendant_preConvictions2preConviction_chargeName=defendants1.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_chargeName");
        String defendants12defendant_preConvictions2preConviction_release=defendants1.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_release");

//        String defendants12charges2suspect_charge_name2index=defendants1.getJSONArray("charges").getJSONObject(0).getJSONObject("suspect_charge_name").getString("index");
//        String defendants12charges2suspect_charge_name2text=defendants1.getJSONArray("charges").getJSONObject(0).getJSONObject("suspect_charge_name").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_type2index0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_type").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_type2text0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_type").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_time2index0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_time").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_time2text0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_time").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_organ2index0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_organ").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_organ2text0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_organ").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_type2index1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_type").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_type2text1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_type").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_time2index1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_time").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_time2text1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_time").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_organ2index1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_organ").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_organ2text1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_organ").getString("text");

        String defendants12defendant_base2NN=defendants1.getJSONObject("defendant_base").getString("NN");

        String defendants12defendant_base2BD2index=defendants1.getJSONObject("defendant_base").getJSONObject("BD").getString("index");
        String defendants12defendant_base2BD2text=defendants1.getJSONObject("defendant_base").getJSONObject("BD").getString("text");

        String defendants12defendant_base2PS = defendants1.getJSONObject("defendant_base").getString("PS");
        String defendants12defendant_base2defendant_nativePlace=defendants1.getJSONObject("defendant_base").getString("defendant_nativePlace");

        String defendants12defendant_base2E2index=defendants1.getJSONObject("defendant_base").getJSONObject("E").getString("index");
        String defendants12defendant_base2E2text=defendants1.getJSONObject("defendant_base").getJSONObject("E").getString("text");

//        String defendants12defendant_base2FN2index=defendants1.getJSONObject("defendant_base").getJSONObject("FN").getString("index");
//        String defendants12defendant_base2FN2text=defendants1.getJSONObject("defendant_base").getJSONObject("FN").getString("text");

        String defendants12defendant_base2defendant_capacity=defendants1.getJSONObject("defendant_base").getString("defendant_capacity");

        String defendants12defendant_base2defendant_occupation2index=defendants1.getJSONObject("defendant_base").getJSONObject("defendant_occupation").getString("index");
        String defendants12defendant_base2defendant_occupation2text=defendants1.getJSONObject("defendant_base").getJSONObject("defendant_occupation").getString("text");

        String defendants12defendant_base2DR2index=defendants1.getJSONObject("defendant_base").getJSONObject("DR").getString("index");
        String defendants12defendant_base2DR2text=defendants1.getJSONObject("defendant_base").getJSONObject("DR").getString("text");

        String defendants12defendant_base2S2index=defendants1.getJSONObject("defendant_base").getJSONObject("S").getString("index");
        String defendants12defendant_base2S2text=defendants1.getJSONObject("defendant_base").getJSONObject("S").getString("text");

        String defendants12defendant_base2defendant_identity=defendants1.getJSONObject("defendant_base").getString("defendant_identity");

//        String defendants12defendant_base2defendant_name2index=defendants1.getJSONObject("defendant_base").getJSONObject("defendant_name").getString("index");
//        String defendants12defendant_base2defendant_name2text=defendants1.getJSONObject("defendant_base").getJSONObject("defendant_name").getString("text");

        String defendants12defendant_base2FA2index=defendants1.getJSONObject("defendant_base").getJSONObject("FA").getString("index");
        String defendants12defendant_base2FA2text=defendants1.getJSONObject("defendant_base").getJSONObject("FA").getString("text");

        String defendants12defendant_base2special_identity2identity_others=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_others");
        String defendants12defendant_base2special_identity2identity_star=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_star");
        String defendants12defendant_base2special_identity2identity_partygroupings=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_partygroupings");
        String defendants12defendant_base2special_identity2identity_intellectual=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_intellectual");
        String defendants12defendant_base2special_identity2identity_cppcc_members=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_cppcc_members");
        String defendants12defendant_base2special_identity2identity_sportstar=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_sportstar");
        String defendants12defendant_base2special_identity2identity_officer=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_officer");
        String defendants12defendant_base2special_identity2identity_npc_member=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_npc_member");
        String defendants12defendant_base2special_identity2identity_foreigner=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_foreigner");
        String defendants12defendant_base2special_identity2identity_taiwan=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_taiwan");
        String defendants12defendant_base2special_identity2identity_leader=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_leader");
        String defendants12defendant_base2special_identity2identity_hongkongmacau=defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_hongkongmacau");


        String defendants12defendant_base2PN2index=defendants1.getJSONObject("defendant_base").getJSONObject("PN").getString("index");
        String defendants12defendant_base2PN2text=defendants1.getJSONObject("defendant_base").getJSONObject("PN").getString("text");

        String defendants12defendant_base2BA=defendants1.getJSONObject("defendant_base").getString("BA");

        String defendants12judgment2penalties2penalty_notimprison=defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_notimprison");
        String defendants12judgment2penalties2penalty_principal=defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_principal");
        String defendants12judgment2penalties2penalty_suspend=defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_suspend");
        String defendants12judgment2penalties2JA=defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("JA");
        String defendants12judgment2penalties2penalty_duration=defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_duration");


        JSONObject penalty_supplementaries1=defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getJSONArray("penalty_supplementaries").getJSONObject(0);
        String defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_money=penalty_supplementaries1.getString("penalty_supplementary_money");
        String defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_content=penalty_supplementaries1.getString("penalty_supplementary_content");
        String defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_type=penalty_supplementaries1.getString("penalty_supplementary_type");

        String defendants12judgment2defendant_joint_crime=defendants1.getJSONObject("judgment").getString("defendant_joint_crime");

        JSONObject attorneys1=defendants1.getJSONArray("attorneys").getJSONObject(0);
        String defendants12attorneys2attorney_type=attorneys1.getString("attorney_type");
        String defendants12attorneys2attorney_workplace=attorneys1.getString("attorney_workplace");
        String defendants12attorneys2attorney_relation=attorneys1.getString("attorney_relation");
        String defendants12attorneys2attorney_name=attorneys1.getString("attorney_name");
        String defendants12attorneys2attorney_occupation=attorneys1.getString("attorney_occupation");

        String defendants12detain2inPrison=defendants1.getJSONObject("detain").getString("inPrison");
        String defendants12detain2inPrison_place=defendants1.getJSONObject("detain").getString("inPrison_place");

        String accusation2accusation_reference=accusation.getString("accusation_reference");
        String accusation2accusation_procurator=accusation.getString("accusation_procurator");
        String accusation2prosecute_charge_name=accusation.getString("prosecute_charge_name");
        String accusation2accusation_time=accusation.getString("accusation_time");
        String accusation2accusation_type=accusation.getString("accusation_type");

        JSONObject laws = judgment.getJSONArray("laws").getJSONObject(0);
        String judgment2laws2law_name=laws.getString("law_name");
        String judgment2laws2article_names=laws.getString("article_names");

        String judgment2judgement_date=laws.getString("judgement_date");

        JSONObject notaffirm_reasons=judgment.getJSONObject("notaffirm_reasons");
        String  judgment2notaffirm_reasons2fact_norelevance=notaffirm_reasons.getString("fact_norelevance");
        String  judgment2notaffirm_reasons2procedure_illegal=notaffirm_reasons.getString("procedure_illegal");
        String  judgment2notaffirm_reasons2fakecopy=notaffirm_reasons.getString("fakecopy");
        String  judgment2notaffirm_reasons2other=notaffirm_reasons.getString("other");
        String  judgment2notaffirm_reasons2evidence_old=notaffirm_reasons.getString("evidence_old");
        String  judgment2notaffirm_reasons2form_illegal=notaffirm_reasons.getString("form_illegal");
        String  judgment2notaffirm_reasons2fakeevidence=notaffirm_reasons.getString("fakeevidence");
        String  judgment2notaffirm_reasons2doubt=notaffirm_reasons.getString("doubt");
        String  judgment2notaffirm_reasons2evidence_unverify=notaffirm_reasons.getString("evidence_unverify");
        String  judgment2notaffirm_reasons2means_illegal=notaffirm_reasons.getString("means_illegal");
        String  judgment2notaffirm_reasons2evidence_flaw=notaffirm_reasons.getString("evidence_flaw");
        String  judgment2notaffirm_reasons2evidence_suspected=notaffirm_reasons.getString("evidence_suspected");

        String document_type=jsonObject.getString("document_type");
        String prosecutors=jsonObject.getString("prosecutors");


        String a=jsonObject.getJSONArray("defendants").getJSONObject(0).getJSONArray("charges").getJSONObject(0).getJSONObject("suspect_charge_name1").getString("a");
        System.out.println(a);

    }
}
