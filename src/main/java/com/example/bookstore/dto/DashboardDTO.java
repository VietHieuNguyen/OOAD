package com.example.bookstore.dto;

import java.util.List;

public class DashboardDTO {

    // Header
    private String greeting;
    private String subtitle;
    private String systemStatusLabel;
    private String systemStatusNote;
    private String currentUserInitials;
    private String currentUserName;
    private String currentUserRole;
    private String currentUserEmail;

    // Metric cards
    private MetricCard revenueCard;
    private MetricCard ordersCard;
    private MetricCard customersCard;
    private MetricCard stockCard;

    // Revenue chart
    private List<RevenueBar> revenueBars;
    private String dataSourceNote;

    // Activity
    private List<ActivityItem> activities;

    // Inventory
    private String inventorySearchPlaceholder;
    private List<BookRow> books;

    // Getters and setters
    public String getGreeting() { return greeting; }
    public void setGreeting(String greeting) { this.greeting = greeting; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getSystemStatusLabel() { return systemStatusLabel; }
    public void setSystemStatusLabel(String systemStatusLabel) { this.systemStatusLabel = systemStatusLabel; }
    public String getSystemStatusNote() { return systemStatusNote; }
    public void setSystemStatusNote(String systemStatusNote) { this.systemStatusNote = systemStatusNote; }
    public String getCurrentUserInitials() { return currentUserInitials; }
    public void setCurrentUserInitials(String currentUserInitials) { this.currentUserInitials = currentUserInitials; }
    public String getCurrentUserName() { return currentUserName; }
    public void setCurrentUserName(String currentUserName) { this.currentUserName = currentUserName; }
    public String getCurrentUserRole() { return currentUserRole; }
    public void setCurrentUserRole(String currentUserRole) { this.currentUserRole = currentUserRole; }
    public String getCurrentUserEmail() { return currentUserEmail; }
    public void setCurrentUserEmail(String currentUserEmail) { this.currentUserEmail = currentUserEmail; }
    public MetricCard getRevenueCard() { return revenueCard; }
    public void setRevenueCard(MetricCard revenueCard) { this.revenueCard = revenueCard; }
    public MetricCard getOrdersCard() { return ordersCard; }
    public void setOrdersCard(MetricCard ordersCard) { this.ordersCard = ordersCard; }
    public MetricCard getCustomersCard() { return customersCard; }
    public void setCustomersCard(MetricCard customersCard) { this.customersCard = customersCard; }
    public MetricCard getStockCard() { return stockCard; }
    public void setStockCard(MetricCard stockCard) { this.stockCard = stockCard; }
    public List<RevenueBar> getRevenueBars() { return revenueBars; }
    public void setRevenueBars(List<RevenueBar> revenueBars) { this.revenueBars = revenueBars; }
    public String getDataSourceNote() { return dataSourceNote; }
    public void setDataSourceNote(String dataSourceNote) { this.dataSourceNote = dataSourceNote; }
    public List<ActivityItem> getActivities() { return activities; }
    public void setActivities(List<ActivityItem> activities) { this.activities = activities; }
    public String getInventorySearchPlaceholder() { return inventorySearchPlaceholder; }
    public void setInventorySearchPlaceholder(String inventorySearchPlaceholder) { this.inventorySearchPlaceholder = inventorySearchPlaceholder; }
    public List<BookRow> getBooks() { return books; }
    public void setBooks(List<BookRow> books) { this.books = books; }

    // --- Inner DTOs ---

    public static class MetricCard {
        private String label;
        private String value;
        private String deltaText;
        private boolean positive;
        private String supportingText;

        public MetricCard() {}
        public MetricCard(String label, String value, String deltaText, boolean positive, String supportingText) {
            this.label = label; this.value = value; this.deltaText = deltaText;
            this.positive = positive; this.supportingText = supportingText;
        }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public String getDeltaText() { return deltaText; }
        public void setDeltaText(String deltaText) { this.deltaText = deltaText; }
        public boolean isPositive() { return positive; }
        public void setPositive(boolean positive) { this.positive = positive; }
        public String getSupportingText() { return supportingText; }
        public void setSupportingText(String supportingText) { this.supportingText = supportingText; }
    }

    public static class RevenueBar {
        private String label;
        private String valueLabel;
        private int height;
        private boolean highlighted;

        public RevenueBar() {}
        public RevenueBar(String label, String valueLabel, int height, boolean highlighted) {
            this.label = label; this.valueLabel = valueLabel;
            this.height = height; this.highlighted = highlighted;
        }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        public String getValueLabel() { return valueLabel; }
        public void setValueLabel(String valueLabel) { this.valueLabel = valueLabel; }
        public int getHeight() { return height; }
        public void setHeight(int height) { this.height = height; }
        public boolean isHighlighted() { return highlighted; }
        public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }
    }

    public static class ActivityItem {
        private String initials;
        private String title;
        private String detail;
        private String meta;
        private String tone; // "positive", "neutral", "warning"

        public ActivityItem() {}
        public ActivityItem(String initials, String title, String detail, String meta, String tone) {
            this.initials = initials; this.title = title; this.detail = detail;
            this.meta = meta; this.tone = tone;
        }
        public String getInitials() { return initials; }
        public void setInitials(String initials) { this.initials = initials; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }
        public String getMeta() { return meta; }
        public void setMeta(String meta) { this.meta = meta; }
        public String getTone() { return tone; }
        public void setTone(String tone) { this.tone = tone; }
    }

    public static class BookRow {
        private String title;
        private String author;
        private String thumbnail;
        private String statusLabel;
        private String statusClass; // "in-stock", "low-stock", "pre-order"
        private String category;
        private String stockLabel;

        public BookRow() {}
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public String getThumbnail() { return thumbnail; }
        public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
        public String getStatusLabel() { return statusLabel; }
        public void setStatusLabel(String statusLabel) { this.statusLabel = statusLabel; }
        public String getStatusClass() { return statusClass; }
        public void setStatusClass(String statusClass) { this.statusClass = statusClass; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getStockLabel() { return stockLabel; }
        public void setStockLabel(String stockLabel) { this.stockLabel = stockLabel; }
    }
}
