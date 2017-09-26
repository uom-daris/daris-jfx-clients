package daris.client.lifepool.dicom.filter;

import daris.client.lifepool.dicom.AttributeTag;

public abstract class AttributeFilterGroup<T> extends FilterGroup {

    public AttributeFilterGroup() {
        super(LogicalOperator.OR, null);
    }

    public void addFilter(AttributeFilter<T> filter) {
        addFilter(filter);
    }

    @Override
    public void addFilter(Filter filter) {
        if (filter == null) {
            return;
        }
        if (!(filter instanceof AttributeFilter)) {
            throw new IllegalArgumentException("Not a AttributeFilter.");
        }
        AttributeFilter<?> af = (AttributeFilter<?>) filter;
        if (!af.tag().equals(tag())) {
            throw new IllegalArgumentException("Unexpected AttributeFilter with tag: " + af.tag());
        }
        super.addFilter(filter);
    }

    public abstract AttributeTag tag();

}
