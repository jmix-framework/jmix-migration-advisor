package io.jmix.migration.analysis.issue.uicomponent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.migration.analysis.issue.uicomponent.UiComponentIssueType.*;

public class UiComponentIssuesRegistry {

    private final Map<String, UiComponentIssue> registry;

    protected UiComponentIssuesRegistry(Map<String, UiComponentIssue> registry) {
        this.registry = registry;
    }

    public static UiComponentIssuesRegistry create() {
        return new UiComponentIssuesRegistry(createRegistry());
    }

    @Nullable
    public UiComponentIssue getIssue(String component) {
        return registry.get(component);
    }

    protected static Map<String, UiComponentIssue> createRegistry() {
        //TODO load from XML
        return Stream.of(
                new UiComponentIssue("appMenu", CHANGED,
                        "Use 'horizontalMenu' (https://docs.jmix.io/jmix/flow-ui/vc/components/horizontalMenu.html)", 3),
                new UiComponentIssue("browserFrame", CHANGED, "Use html 'iframe' (https://docs.jmix.io/jmix/flow-ui/vc/html.html)", 1),
                new UiComponentIssue("bulkEditor", CHANGED, "BulkEditor component is legacy. Add Jmix add-on 'Bulk Editor' and use 'bulkEdit' action (https://docs.jmix.io/jmix/bulk-edit/index.html)", 3),
                new UiComponentIssue("calendar", ABSENT, "Currently in progress"),
                new UiComponentIssue("capsLockIndicator", ABSENT, "Not implemented"),
                new UiComponentIssue("colorPicker", CHANGED, "Use html 'input' with type = 'color' (https://docs.jmix.io/jmix/flow-ui/vc/html.html)", 1),
                new UiComponentIssue("currencyField", HAS_ALTERNATIVE,
                        "Use 'textField' with prefix (https://docs.jmix.io/jmix/flow-ui/vc/components/textField.html). But there is no integration with '@CurrencyValue' annotation", 1),
                new UiComponentIssue("embedded", CHANGED, "Legacy API. Use 'image' or 'iframe'", 2),
                new UiComponentIssue("fieldGroup", CHANGED, "Legacy API. Use 'formLayout'", 2),
                new UiComponentIssue("fileMultiUploadField", CHANGED, "Legacy API. Use 'upload' with 'MULTI_FILE_*' receiver type (https://docs.jmix.io/jmix/flow-ui/vc/components/upload.html)", 2),
                new UiComponentIssue("groupTable", ABSENT, "Not implemented yet"),
                new UiComponentIssue("link", CHANGED, "Use html 'anchor' (https://docs.jmix.io/jmix/flow-ui/vc/html.html)", 1),
                new UiComponentIssue("linkButton", HAS_ALTERNATIVE, "Use 'button' with custom styles (https://docs.jmix.io/jmix/flow-ui/vc/components/button.html#variants)", 1),
                new UiComponentIssue("maskedField", ABSENT, "Not implemented. TextField supports input restriction via regex pattern (https://docs.jmix.io/jmix/flow-ui/vc/common-attributes.html#allowedCharPattern)"),
                new UiComponentIssue("optionsGroup", HAS_ALTERNATIVE, "Use 'checkBoxGroup' or 'radioButtonGroup' directly", 1),
                new UiComponentIssue("optionsList", HAS_ALTERNATIVE, "Use 'listBox' or 'multiSelectListBox'", 2),
                new UiComponentIssue("popupView", ABSENT, "Use 'dialog' with custom layout (https://docs.jmix.io/jmix/flow-ui/dialogs.html)", 5),
                new UiComponentIssue("relatedEntities", ABSENT, "Use 'dropdownButton' with manual screen opening action", 5),
                new UiComponentIssue("resizableTextArea", CHANGED, "Use 'textArea' with resize feature programmatically configured via styles (https://demo.jmix.io/ui-samples/sample/text-area-resizable)", 1),
                new UiComponentIssue("searchPickerField", HAS_ALTERNATIVE, "Legacy API. Use 'comboBox'/'entityComboBox' with 'itemsQuery' parameter (https://demo.jmix.io/ui-samples/sample/entity-combobox-items-query)", 2),
                new UiComponentIssue("sideMenu", CHANGED, "Use 'listMenu'", 3),
                new UiComponentIssue("slider", ABSENT, "Create custom component using 3rd-party JS library (https://demo.jmix.io/ui-samples/sample/custom-component-js-library)", 3),
                new UiComponentIssue("suggestionField", CHANGED, "Use 'comboBox'. It works with suggestion by default for non-entity values. See also its 'itemsQuery' parameter (https://demo.jmix.io/ui-samples/sample/combobox-items-query)", 2),
                new UiComponentIssue("suggestionPickerField", CHANGED, "Use 'comboBox'/'entityComboBox' with 'itemsQuery' parameter (https://demo.jmix.io/ui-samples/sample/entity-combobox-items-query)"),
                new UiComponentIssue("tokenList", HAS_ALTERNATIVE, "Use 'multiValuePicker', 'multiSelectComboBox' or 'multiSelectComboBoxPicker'", 2),
                new UiComponentIssue("flowBox", HAS_ALTERNATIVE, "Use 'flexLayout'", 3),
                new UiComponentIssue("buttonsPanel", HAS_ALTERNATIVE, "Use 'hbox' with className 'buttons-panel'", 1),
                new UiComponentIssue("cssLayout", CHANGED, "Supported by all layouts"),
                new UiComponentIssue("frame", CHANGED, "Legacy API. Use 'fragment' (https://docs.jmix.io/jmix/flow-ui/fragments/fragments.html)", 5),
                new UiComponentIssue("grid", CHANGED, "Use 'formLayout'", 2),
                new UiComponentIssue("htmlBox", HAS_ALTERNATIVE, "Use 'html' (https://docs.jmix.io/jmix/flow-ui/vc/components/html.html)", 3)
        ).collect(Collectors.toUnmodifiableMap(UiComponentIssue::getComponent, Function.identity()));
    }
}
