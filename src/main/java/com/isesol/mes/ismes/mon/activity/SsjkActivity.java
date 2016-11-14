package com.isesol.mes.ismes.mon.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.isesol.ismes.platform.module.Bundle;
import com.isesol.ismes.platform.module.Parameters;
import com.isesol.ismes.platform.module.Sys;

public class SsjkActivity {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat sdf_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**
	 * 跳转进度跟踪页面
	 * 
	 * @param parameters
	 * @param bundle
	 * @return
	 */
	public String query_ssjk(Parameters parameters, Bundle bundle) {
		
		Bundle b_sbxx = Sys.callModuleService("fm", "fmService_sbtb", parameters);
		bundle.put("nodes", b_sbxx.get("nodes"));
		bundle.put("toolBtns", b_sbxx.get("toolBtns"));
		bundle.put("data", b_sbxx.get("data"));
		
		return "fm_sbxx";
	}
	
	/**设备实时监控
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	public String query_sbssjk(Parameters parameters, Bundle bundle) throws Exception {
//		A131420099
		//处理时间参数开始
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.SECOND, -5);
		date = c.getTime();
		parameters.set("querytime", date );
		
		Date date_start = sdf_time.parse(sdf.format(new Date())+" 00:00:00");
//		Date date_end = sdf_time.parse(sdf.format(new Date())+" 23:59:59");
		Date date_end = new Date();
		parameters.set("date_start", date_start);
		parameters.set("date_end",  date);
		//处理时间参数结束
		
		Map<String, Object> mapsbjk = new HashMap<String, Object>();
		
		
		//查询累计时间
		long ljyx = 0;
		long ljkx = 0;
		long ljgz = 0;
		long ljtj = 0;
		if(null!=parameters.get("sbbh"))
		{
			Bundle b_sbxx = Sys.callModuleService("em", "emservice_sbxxFile", parameters);
			Map<String, Object> map_sb = (Map<String, Object>) b_sbxx.get("sbxx");
			
			Parameters p_jgdy = new Parameters();
			p_jgdy.set("sbid", map_sb.get("sbid"));
			Bundle bundle_jgdy = Sys.callModuleService("em", "emservice_jgdyBySblxids", p_jgdy);
			List<Map<String, Object>> rows  = (List<Map<String, Object>>) bundle_jgdy.get("data");
			String jgdyid = "";
			if(CollectionUtils.isNotEmpty(rows)){
				jgdyid = rows.get(0).get("jgdyid").toString();
			}
			//查询工单信息
			parameters.set("sbid",  jgdyid);
			Bundle b_gdxx = Sys.callModuleService("pl", "plservice_query_gdxxBysbid", parameters);
			
			if(null != b_gdxx)
			{
				List<Map<String, Object>> gdxx = (List<Map<String, Object>>) b_gdxx.get("gdxxList");
				
				if(gdxx.size()>0)
				{
					mapsbjk = gdxx.get(0);
					parameters.set("scrwpcid", (mapsbjk.get("pcid")));
					Bundle b_scrwxx = Sys.callModuleService("pro", "scrwAndPcInfoByPcidService", parameters);
					Map<String, Object> map = (Map<String, Object>) b_scrwxx.get("scrwandpc");
					mapsbjk.put("scrwbh", map.get("scrwbh"));
					mapsbjk.put("pcbh", map.get("pcbh"));
					mapsbjk.put("wcjd",""+(Math.round((Integer.parseInt(mapsbjk.get("gdywcsl").toString())*10000)/Integer.parseInt(mapsbjk.get("jgsl").toString()))/100.0));
					
					parameters.set("val_lj", "('"+mapsbjk.get("ljid")+"')");
					Bundle b_ljxx = Sys.callModuleService("pm", "pmservice_ljxx", parameters);
					if(null==b_ljxx)
					{
						bundle.put("gdxx", mapsbjk);
						return "json:gdxx";
					}
					List<Map<String, Object>> ljxx = (List<Map<String, Object>>) b_ljxx.get("ljxx");
					if(ljxx.size()>0){
						mapsbjk.put("ljmc", ljxx.get(0).get("ljmc"));
					}
				}else{
					mapsbjk.put("scrwbh","无");
					mapsbjk.put("pcbh","无");
					mapsbjk.put("wcjd",0);
					mapsbjk.put("ljmc","无");
					mapsbjk.put("gdbh","无");
					mapsbjk.put("jgsl","无");
					mapsbjk.put("jgkssj","无");
					mapsbjk.put("gdywcsl","无");
				}
			}
			mapsbjk.put("sbbh", map_sb.get("sbbh"));
			mapsbjk.put("sbph", map_sb.get("cj").toString() +map_sb.get("sblxid").toString());
			mapsbjk.put("url", map_sb.get("url"));
			

			Bundle b_ljsj = Sys.callModuleService("interf", "ifService_ljsj", parameters);
			List<Map<String, Object>> ljsj_zzt = new ArrayList<Map<String,Object>>();
			if(null != b_ljsj)
			{
				List<Map<String, Object>> ljsj = (List<Map<String, Object>>) b_ljsj.get("ljsj");
				
				
				for (int i = 0; i < ljsj.size(); i++)
				{
					Map<String, Object> mapzzt = new HashMap<String, Object>();
					Date ztkssj = ljsj.get(i).get("sjkssj") ==null?new Date():sdf_time.parse(ljsj.get(i).get("sjkssj").toString());
					Date ztjssj = ljsj.get(i).get("sjjssj") ==null?new Date():sdf_time.parse(ljsj.get(i).get("sjjssj").toString());
					if(ztkssj.getTime()<date_start.getTime())
					{
						ztkssj = date_start;
						mapzzt.put("value", date_start);
						mapzzt.put("sbztdm", ljsj.get(i).get("sbztdm")); 
						ljsj_zzt.add(mapzzt);
						mapzzt = new HashMap<String, Object>();
					}else if(ztjssj.getTime()>date_end.getTime())
					{
						ztjssj = date_end;
					}
					mapzzt.put("value", ztjssj); 
					mapzzt.put("sbztdm", ljsj.get(i).get("sbztdm")); 
					ljsj_zzt.add(mapzzt);
					
					long sjc = ztjssj.getTime() -  ztkssj.getTime();
					if ("10".equals(ljsj.get(i).get("sbztdm"))) {
						ljkx = ljkx + sjc;
					} else if ("20".equals(ljsj.get(i).get("sbztdm"))) {
						ljyx = ljyx + sjc;
					} else if ("30".equals(ljsj.get(i).get("sbztdm"))) {
						ljgz = ljgz + sjc;
					} else if ("40".equals(ljsj.get(i).get("sbztdm"))) {
						ljtj = ljtj + sjc;
					}
				}
			}
			mapsbjk.put("ljyx", formatDuring_h(ljyx));
			mapsbjk.put("ljkx", formatDuring_h(ljkx));
			mapsbjk.put("ljgz", formatDuring_h(ljgz));
			mapsbjk.put("ljtj", formatDuring_h(ljtj));
			mapsbjk.put("ljsj_zzt",ljsj_zzt);
			
			Bundle b_ssxx = Sys.callModuleService("interf", "ifService_ssxx", parameters);
			Map<String, Object> mapssxx =   (Map<String, Object>) b_ssxx.get("mapsbjk");
			if ("10".equals(mapssxx.get("sbztdm"))) {
				mapsbjk.put("sbztdmname","空闲");
				mapsbjk.put("sbztdmurl",Sys.getResourceUrl("mon","sbztkx"));
			} else if ("20".equals(mapssxx.get("sbztdm"))) {
				mapsbjk.put("sbztdmname","运行");
				mapsbjk.put("sbztdmurl",Sys.getResourceUrl("mon","sbztyx"));
			} else if ("30".equals(mapssxx.get("sbztdm"))) {
				mapsbjk.put("sbztdmname","故障");
				mapsbjk.put("sbztdmurl",Sys.getResourceUrl("mon","sbztgz"));
			} else  {//if ("40".equals(mapssxx.get("sbztdm")))
				mapsbjk.put("sbztdmname","停机");
				mapsbjk.put("sbztdmurl",Sys.getResourceUrl("mon","sbzttj"));
			}
			if("error".equals(mapssxx.get("msg")))
			{
				mapsbjk.put("zzzsz_s","0");
				mapsbjk.put("zzfzz","0");
				mapsbjk.put("zzplz","0");
				mapsbjk.put("zjsdz_f","0");
				mapsbjk.put("xaxis","0");
				mapsbjk.put("yaxis","0");
				mapsbjk.put("zaxis","0");
			}else{
				mapsbjk.put("zzzsz_s",mapssxx.get("zzzsz_s"));
				mapsbjk.put("zzfzz",mapssxx.get("zzfzz"));
				mapsbjk.put("zjsdz_f",mapssxx.get("zjsdz_f"));
				mapsbjk.put("zzplz",Float.valueOf(mapssxx.get("zzplz").toString())*100 );
				mapsbjk.put("xaxis",mapssxx.get("xaxis"));
				mapsbjk.put("yaxis",mapssxx.get("yaxis"));
				mapsbjk.put("zaxis",mapssxx.get("zaxis"));
			}
			
			if("success".equals(mapssxx.get("gzxx")))
			{
				mapsbjk.put("gzm",mapssxx.get("gzm"));
				mapsbjk.put("gznr","无");
				mapsbjk.put("gzfssj",mapssxx.get("gzfssj"));
			}else{
				mapsbjk.put("gzm","无");
				mapsbjk.put("gznr","无");
				mapsbjk.put("gzfssj","无");
			}
		}
		bundle.put("mapsbjk", mapsbjk);
		return "json:mapsbjk";
	}
	/**停机信息列表
	 * @param parameters
	 * @param bundle
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public String table_tjxx(Parameters parameters, Bundle bundle) throws Exception {
		if(null==parameters.get("sbbh"))
		{
			return "json:";
		}
		parameters.set("sbztdm", "40");
		Bundle b_tjxx = Sys.callModuleService("interf", "ifService_tjxx", parameters);
		if(null == b_tjxx)
		{
			return "json:";
		}
		List<Map<String, Object>> tjxx = (List<Map<String, Object>>) b_tjxx.get("tjxx");
		for (int i = 0; i < tjxx.size(); i++)
		{
			
			Date ztkssj = tjxx.get(i).get("sjkssj") ==null?new Date():sdf_time.parse(tjxx.get(i).get("sjkssj").toString());
			Date ztjssj = tjxx.get(i).get("sjjssj") ==null?new Date():sdf_time.parse(tjxx.get(i).get("sjjssj").toString());
			tjxx.get(i).put("tjsj", formatDuring_h(ztjssj.getTime() - ztkssj.getTime())+"小时") ;
			tjxx.get(i).put("tjyy", "未知") ;
		}
		bundle.put("rows", tjxx);
		bundle.put("totalPage", b_tjxx.get("totalPage"));
		bundle.put("currentPage",b_tjxx.get("currentPage"));
		bundle.put("totalRecord", b_tjxx.get("totalRecord"));
		return "json:";
	}
	
	/**毫秒数转成时间显示
	 * @param mss
	 * @return
	 */
	public static String formatDuring(long mss) {  
		long days = mss / (1000 * 60 * 60 * 24);  
		long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);  
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);  
		long seconds = (mss % (1000 * 60)) / 1000;
		String sjc = "";
		if(0<days)
		{
			sjc = sjc + days + " 天 ";
		}
		if(0<hours)
		{
			sjc = sjc + hours + " 时 ";
		}
		if(0<minutes)
		{
			sjc = sjc + minutes + " 分 ";
		}
		if(0<seconds)
		{
			sjc = sjc + seconds + " 秒  ";
		}
		if(StringUtils.isBlank(sjc))
		{
			sjc = "0 秒";
		}
		return  sjc;  
	}  
	public static String formatDuring_h(long mss) {
		long hours = mss / (1000 * 60 * 60);  
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);  
		if(minutes>0)
		{
			return  ""+(hours+(Math.round(minutes*100/60)/100.0));  
		}else{
			return  ""+hours;  
		}
		
	}  
}




