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
                new UiComponentIssue("calendar", CHANGED, "Add Jmix add-on 'Calendar' (jmix-fullcalendar) and use 'calendar' component (https://docs.jmix.io/jmix/calendar/index.html)", 3),
                new UiComponentIssue("capsLockIndicator", ABSENT, "Not implemented. No built-in caps lock indicator in Jmix Flow UI; if needed, implement a custom component detecting Caps Lock via client-side JS (KeyboardEvent.getModifierState('CapsLock'))"),
                new UiComponentIssue("colorPicker", HAS_ALTERNATIVE, "No dedicated component. Use html 'input' with type='color' for a native color picker (https://docs.jmix.io/jmix/flow-ui/vc/html.html). The plain tag is not value-source bindable; for data binding wrap it in a small custom component", 1),
                new UiComponentIssue("currencyField", HAS_ALTERNATIVE,
                        "Use 'textField' with prefix (https://docs.jmix.io/jmix/flow-ui/vc/components/textField.html). But there is no integration with '@CurrencyValue' annotation", 1),
                new UiComponentIssue("embedded", CHANGED, "Legacy API. Use 'image' for images (https://docs.jmix.io/jmix/flow-ui/vc/html-components/image.html), html 'iframe' for embedded web pages, or 'htmlObject' for embedded objects", 2),
                new UiComponentIssue("fieldGroup", CHANGED, "Legacy API. Use 'formLayout'", 2),
                new UiComponentIssue("fileMultiUploadField", CHANGED, "Legacy API. Use 'upload' with 'MULTI_FILE_*' receiver type (https://docs.jmix.io/jmix/flow-ui/vc/components/upload.html)", 2),
                new UiComponentIssue("groupTable", CHANGED, "Add the commercial 'Grouping Data Grid' add-on (io.jmix.groupgrid, requires Enterprise subscription) and use 'groupDataGrid' (https://docs.jmix.io/jmix/groupdatagrid/index.html)", 3),
                new UiComponentIssue("link", CHANGED, "Use html 'anchor' (https://docs.jmix.io/jmix/flow-ui/vc/html.html)", 1),
                new UiComponentIssue("linkButton", HAS_ALTERNATIVE, "Use 'button' with themeNames=\"tertiary-inline\" for a borderless, link-style button (https://docs.jmix.io/jmix/flow-ui/vc/components/button.html#variants)", 1),
                new UiComponentIssue("maskedField", HAS_ALTERNATIVE, "No built-in masked input. Use the 3rd-party Vaadin 'Input Mask' add-on: apply programmatically via new InputMask(\"...\").extend(field) (~2 lines), or wrap it in a small custom component for declarative XML use (https://demo.jmix.io/ui-samples/sample/input-mask-addon-advanced). For plain character restriction without formatting, use textField 'allowedCharPattern'", 2),
                new UiComponentIssue("optionsGroup", CHANGED, "Use 'checkboxGroup' for multi-select or 'radioButtonGroup' for single-select", 1),
                new UiComponentIssue("optionsList", CHANGED, "Use 'listBox' for single selection or 'multiSelectListBox' for multiple selection", 2),
                new UiComponentIssue("popupView", ABSENT, "No direct equivalent. Open a 'dialog' with custom layout (https://docs.jmix.io/jmix/flow-ui/dialogs.html); 'sideDialog' and 'sidePanelLayout' are additional alternatives", 5),
                new UiComponentIssue("relatedEntities", ABSENT, "Use 'dropdownButton' with manual screen opening action", 5),
                new UiComponentIssue("resizableTextArea", HAS_ALTERNATIVE, "Use 'textArea' and enable resizing via a CSS class applying the native 'resize' property (https://demo.jmix.io/ui-samples/sample/text-area-resizable). No built-in resizableDirection attribute or resize event", 1),
                new UiComponentIssue("searchPickerField", CHANGED, "Legacy API. Use 'entityComboBox' with nested 'itemsQuery' for query-driven lazy search (also supports picker actions); use 'comboBox' + 'itemsQuery' for non-entity values (https://docs.jmix.io/jmix/flow-ui/vc/components/entityComboBox.html)", 2),
                new UiComponentIssue("sideMenu", CHANGED, "Use 'listMenu'", 3),
                new UiComponentIssue("slider", HAS_ALTERNATIVE, "Use HTML 'rangeInput' (an <input type='range'> supporting min/max/step/orientation, value type Double) for a basic range/slider input. It is not declaratively bindable to a data container - bind its value to the entity attribute manually in the controller. For a full-featured slider (tooltips, ticks, multiple handles), create a custom component with a 3rd-party JS library", 2),
                new UiComponentIssue("suggestionField", CHANGED, "Use 'comboBox'. It works with suggestion by default for non-entity values. See also its 'itemsQuery' parameter (https://demo.jmix.io/ui-samples/sample/combobox-items-query)", 2),
                new UiComponentIssue("suggestionPickerField", CHANGED, "Legacy API. Use 'entityComboBox' with nested 'itemsQuery' for search-as-you-type entity selection with picker actions (https://docs.jmix.io/jmix/flow-ui/vc/components/entityComboBox.html)", 2),
                new UiComponentIssue("tokenList", CHANGED, "Use 'multiSelectComboBox' (chips), 'multiSelectComboBoxPicker' (with action buttons) or 'multiValuePicker' (https://docs.jmix.io/jmix/flow-ui/vc/components/multiSelectComboBox.html)", 2),
                new UiComponentIssue("flowBox", CHANGED, "Use 'flexLayout' with flexWrap=\"WRAP\" (https://docs.jmix.io/jmix/flow-ui/vc/layouts/flexLayout.html)", 2),
                new UiComponentIssue("buttonsPanel", CHANGED, "Use 'hbox' with classNames=\"buttons-panel\" (https://docs.jmix.io/jmix/flow-ui/vc/layouts/hbox.html)", 1),
                new UiComponentIssue("cssLayout", CHANGED, "Use 'flexLayout' for a CSS-driven container or 'div' for a plain styled container; CSS styling via 'classNames'/'css' is supported by all layouts (https://docs.jmix.io/jmix/flow-ui/vc/layouts/flexLayout.html)"),
                new UiComponentIssue("frame", CHANGED, "Legacy API. Use 'fragment' (https://docs.jmix.io/jmix/flow-ui/fragments/fragments.html)", 5),
                new UiComponentIssue("grid", CHANGED, "Use 'gridLayout' (https://docs.jmix.io/jmix/flow-ui/vc/layouts/gridLayout.html)", 2),
                new UiComponentIssue("htmlBox", HAS_ALTERNATIVE, "Use 'html' to render HTML markup as string/CDATA or from a file (https://docs.jmix.io/jmix/flow-ui/vc/components/html.html). It does not support HtmlBoxLayout component 'location' placeholders; re-lay-out nested components with standard layout containers", 3)
        ).collect(Collectors.toUnmodifiableMap(UiComponentIssue::getComponent, Function.identity()));
    }
}
