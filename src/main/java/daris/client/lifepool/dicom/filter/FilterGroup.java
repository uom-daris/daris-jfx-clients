package daris.client.lifepool.dicom.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FilterGroup implements Filter {

    private LogicalOperator _logicalOp;
    private List<Filter> _filters;

    public FilterGroup(LogicalOperator logicalOperator, Collection<Filter> filters) {
        _logicalOp = logicalOperator;
        if (filters != null && !filters.isEmpty()) {
            _filters = new ArrayList<Filter>(filters.size());
            _filters.addAll(filters);
        }
    }

    public LogicalOperator logicalOperator() {
        return _logicalOp;
    }

    public void addFilter(Filter filter) {
        if (_filters == null) {
            _filters = new ArrayList<Filter>();
        }
        _filters.add(filter);
    }

    public void removeFilter(Filter filter) {
        if (_filters != null) {
            _filters.remove(filter);
        }
    }

    public List<Filter> filters() {
        if (_filters == null || _filters.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableList(_filters);
    }

    public int size() {
        return _filters == null ? 0 : _filters.size();
    }

    @Override
    public void save(StringBuilder sb) {
        sb.append(" ");
        int nbFilters = size();
        if (nbFilters <= 0) {
            sb.append("id>0");
            return;
        }
        if (nbFilters > 1) {
            sb.append("(");
        }
        for (int i = 0; i < nbFilters; i++) {
            Filter filter = _filters.get(i);
            if (i > 0) {
                sb.append(" ").append(logicalOperator()).append(" ");
            }
            filter.save(sb);
        }
        if (nbFilters > 1) {
            sb.append(")");
        }
    }
}
