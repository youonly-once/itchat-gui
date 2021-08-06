package cn.shu.wechat.utils;

import cn.shu.wechat.beans.pojo.AttrHistory;
import cn.shu.wechat.mapper.AttrHistoryMapper;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.util.HashSet;
import java.util.List;

/**
 * @author SXS
 * @since 4/13/2021
 */
@Log4j2
public class HeadImageUtil {

    /**
     * 删除下载的失效头像
     */
    public static void deleteLoseEfficacyHeadImg(String imgPath) {
        AttrHistoryMapper attrHistoryMapper = SpringContextHolder.getBean(AttrHistoryMapper.class);
        List<AttrHistory> headImageList = attrHistoryMapper
                .selectByAll(AttrHistory.builder()
                        .attr("头像更换")
                        .build());
        HashSet<String> headImages = new HashSet<>();
        for (AttrHistory attrHistory : headImageList) {
            headImages.add(attrHistory.getNewval());
            headImages.add(attrHistory.getOldval());
        }
        deleteFile(imgPath, headImages);
        log.info("头像删除成功");

    }

    /**
     * 遍历删除文件
     *
     * @param imgPath    目录
     * @param headImages 不删除列表
     */
    private static void deleteFile(String imgPath, HashSet<String> headImages) {
        File file = new File(imgPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File file1 : files) {
                if (file1.isFile()) {
                    if (!headImages.contains(file1.getAbsolutePath())) {
                        file1.delete();
                    }

                } else if (file1.isDirectory()) {
                    deleteFile(file1.getAbsolutePath(), headImages);
                }

            }
        }
    }
}
