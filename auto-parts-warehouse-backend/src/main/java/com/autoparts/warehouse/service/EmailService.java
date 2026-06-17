package com.autoparts.warehouse.service;

import com.autoparts.warehouse.entity.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    /**
     * 发送库存预警邮件（单个商品）
     *
     * @param toEmail        收件人邮箱
     * @param partName       配件名称
     * @param currentStock   当前库存
     * @param safeStock      安全库存
     */
    public void sendStockAlertEmail(String toEmail, String partName, Integer currentStock, Integer safeStock) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(toEmail);
        message.setSubject("【库存预警】配件库存低于安全库存提醒");
        
        String content = String.format(
            "%s 商品 目前库存%d，低于安全库存（%d），请及时补充库存。",
            partName, currentStock, safeStock
        );
        message.setText(content);
        
        try {
            mailSender.send(message);
            System.out.println("库存预警邮件发送成功: " + partName);
        } catch (Exception e) {
            System.err.println("库存预警邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 批量发送库存预警邮件（多个商品汇总到一封邮件）
     *
     * @param toEmail       收件人邮箱
     * @param lowStockList  库存不足的商品列表
     */
    public void sendBatchStockAlertEmail(String toEmail, List<Stock> lowStockList) {
        if (lowStockList == null || lowStockList.isEmpty()) {
            System.out.println("没有库存不足的商品，无需发送邮件");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(toEmail);
        message.setSubject("【库存预警】库存不足商品汇总提醒");

        StringBuilder content = new StringBuilder();
        content.append("尊敬的管理员：\n\n");
        content.append("以下商品库存已低于安全库存，请及时补充：\n\n");
        content.append("--------------------------------------------------------\n");
        content.append(String.format("%-20s %-12s %-12s\n", "商品名称", "当前库存", "安全库存"));
        content.append("--------------------------------------------------------\n");

        for (Stock stock : lowStockList) {
            content.append(String.format("%-20s %-12d %-12d\n", 
                truncate(stock.getPartName(), 20), 
                stock.getQuantity(), 
                stock.getSafeStock()));
        }

        content.append("--------------------------------------------------------\n\n");
        content.append("请尽快安排采购补充库存。");

        message.setText(content.toString());

        try {
            mailSender.send(message);
            System.out.println("批量库存预警邮件发送成功，共 " + lowStockList.size() + " 件商品");
        } catch (Exception e) {
            System.err.println("批量库存预警邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 发送出库预警邮件（出库时库存不足提醒）
     *
     * @param toEmail       收件人邮箱
     * @param partName      配件名称
     * @param currentStock  当前库存（出库前）
     * @param outboundQty   出库数量
     * @param safeStock     安全库存
     */
    public void sendOutboundAlertEmail(String toEmail, String partName, Integer currentStock, Integer outboundQty, Integer safeStock) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(toEmail);
        message.setSubject("【出库预警】配件库存不足提醒");

        StringBuilder content = new StringBuilder();
        content.append("尊敬的管理员：\n\n");
        content.append(String.format("%s 商品 目前库存%d，本次出库%d，", partName, currentStock, outboundQty));
        
        int remainingStock = currentStock - outboundQty;
        if (remainingStock <= safeStock) {
            content.append(String.format("出库后库存%d，低于安全库存（%d），请及时补充库存。", remainingStock, safeStock));
        } else {
            content.append("出库后库存充足。");
        }

        message.setText(content.toString());

        try {
            mailSender.send(message);
            System.out.println("出库预警邮件发送成功: " + partName);
        } catch (Exception e) {
            System.err.println("出库预警邮件发送失败: " + e.getMessage());
        }
    }

    /**
     * 截断字符串到指定长度
     */
    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 1) + "…";
    }
}
