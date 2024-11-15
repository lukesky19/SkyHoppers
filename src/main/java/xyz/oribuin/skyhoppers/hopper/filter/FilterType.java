package xyz.oribuin.skyhoppers.hopper.filter;

public enum FilterType {
    NONE("No items will be filted and all items will be allowed in."),
    WHITELIST("Only specific items are allowed in."),
    BLACKLIST("Deny specific items from going in."),
    DESTROY("Destroy specific items the hopper picks up.");

    private final String desc;

    FilterType(final String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}
