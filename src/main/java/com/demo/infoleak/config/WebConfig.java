package com.demo.infoleak.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 鍏佽鐩存帴璁块棶涓婁紶鐩綍涓殑鏂囦欢
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(0);

        // ===== 婕忔礊锛氭毚闇?.git 鐩綍 =====
        // 鏀诲嚮鑰呭彲閫氳繃 /.git/HEAD銆?.git/config 绛夎矾寰勮幏鍙栧叏閮ㄦ簮鐮佸拰鎻愪氦鍘嗗彶
        // 娉細.git_backup 鍖呭惈妯℃嫙 git 鍘嗗彶鏁版嵁锛岀敤浜庢紨绀?        // 瀹為檯鐜涓敾鍑昏€呬細璁块棶鐪熷疄鐨?.git 鐩綍
        registry.addResourceHandler("/.git/**")
                .addResourceLocations("file:./.git/")
                .setCachePeriod(0);
    }
}
