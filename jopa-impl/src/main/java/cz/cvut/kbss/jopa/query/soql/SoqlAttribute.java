/**
 * Copyright (C) 2022 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jopa.query.soql;

public class SoqlAttribute extends SoqlParameter {

    private String value;

    private boolean isNot = false;

    private String operator;

    private boolean isOrderBy = false;

    private boolean isGroupBy = false;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isNot() {
        return isNot;
    }

    public void setNot(boolean not) {
        isNot = not;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

    public boolean isOrderBy() {
        return isOrderBy;
    }

    public void setOrderBy(boolean orderBy) {
        isOrderBy = orderBy;
    }

    public boolean isGroupBy() {
        return isGroupBy;
    }

    public void setGroupBy(boolean groupBy) {
        isGroupBy = groupBy;
    }

    public boolean isFilter() {
        return !operator.isEmpty() && !"=".equals(operator);
    }

    public boolean isObject() {
        return !getFirstNode().hasNextChild();
    }

    public String getFilter() {
        // TODO Refactor into class hierarchy to prevent this if-else tree
        StringBuilder buildFilter = new StringBuilder();
        if (SoqlConstants.LIKE.equals(operator)) {
            buildFilter.append("regex(").append(getAsParam()).append(", ").append(toVariable(value))
                       .append(") ");
        } else if (SoqlConstants.IN.equals(operator) || SoqlConstants.NOT_IN.equals(operator)) {
            buildFilter.append(getAsParam()).append(" ").append(this.operator).append(" (").append(toVariable(value)).append(')');
        } else {
            buildFilter.append(getAsParam()).append(" ").append(this.operator).append(" ").append(toVariable(value));
        }
        return buildFilter.toString();
    }

    public String getTriplePattern() {
        StringBuilder buildTP = new StringBuilder("?x ");
        if (isObject()) {
            buildTP.append(SoqlConstants.RDF_TYPE).append(" ")
                   .append(toIri(getFirstNode())).append(" . ");
        } else {
            SoqlNode pointer = getFirstNode().getChild();
            StringBuilder buildParam = new StringBuilder("?");
            buildParam.append(getFirstNode().getValue());
            buildParam.append(pointer.getCapitalizedValue());
            String param;
            if (pointer.hasNextChild()) {
                param = "?" + pointer.getValue();
            } else {
                if (isFilter()) {
                    param = buildParam.toString();
                } else {
                    param = toVariable(value);
                }
            }
            buildTP.append(toIri(pointer)).append(" ").append(param).append(" . ");
            while (pointer.hasNextChild()) {
                SoqlNode newPointer = pointer.getChild();
                buildTP.append("?").append(pointer.getValue())
                       .append(" ").append(toIri(newPointer)).append(" ");
                buildParam.append(newPointer.getCapitalizedValue());
                if (newPointer.hasNextChild()) {
                    buildTP.append("?").append(pointer.getChild().getValue());
                } else {
                    if (isFilter()) {
                        buildTP.append(buildParam);
                    } else {
                        buildTP.append(toVariable(value));
                    }
                }
                buildTP.append(" . ");
                pointer = newPointer;
            }
        }
        return buildTP.toString();
    }

    private StringBuilder toIri(SoqlNode node) {
        StringBuilder sb = new StringBuilder("<");
        String prefix = node.getIri().isEmpty() ? node.getValue() : node.getIri();
        sb.append(prefix).append(">");
        return sb;
    }

    private static String toVariable(String name) {
        return "?" + name.substring(1);
    }
}
