package io.jmix.migration.analysis.issue.uicomponent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                UiComponentIssue.createChanged("appMenu",
                        "Use 'horizontalMenu' (https://docs.jmix.io/jmix/flow-ui/vc/components/horizontalMenu.html)", 3),
                UiComponentIssue.createChanged("browserFrame", "Use html 'iframe' (https://docs.jmix.io/jmix/flow-ui/vc/html.html)", 1),
                UiComponentIssue.createChanged("bulkEditor", "BulkEditor component is legacy. Add Jmix add-on 'Bulk Editor' and use the 'bulked_edit' action (https://docs.jmix.io/jmix/bulk-edit/index.html)", 3,
                        Requires.addon("Bulk Editor (io.jmix.bulkeditor)")),
                UiComponentIssue.createChanged("calendar", "Add Jmix add-on 'Calendar' (jmix-fullcalendar) and use 'calendar' component (https://docs.jmix.io/jmix/calendar/index.html)", 3,
                        Requires.addon("Calendar (io.jmix.fullcalendar)")),
                UiComponentIssue.createAbsent("capsLockIndicator", "Not implemented. No built-in caps lock indicator in Jmix Flow UI; if needed, implement a custom component detecting Caps Lock via client-side JS (KeyboardEvent.getModifierState('CapsLock'))"),
                UiComponentIssue.createAlternative("colorPicker", "No dedicated component. Use html 'input' with type='color' for a native color picker (https://docs.jmix.io/jmix/flow-ui/vc/html.html). The plain tag is not value-source bindable; for data binding wrap it in a small custom component", 1),
                UiComponentIssue.createAlternative("currencyField",
                        "Use 'textField' with prefix (https://docs.jmix.io/jmix/flow-ui/vc/components/textField.html). But there is no integration with '@CurrencyValue' annotation", 1),
                UiComponentIssue.createAlternative("datePicker", "CUBA datePicker is an inline (always visible) calendar. Jmix Flow UI 'datePicker' is a popup date input field with the same name but different UX (https://docs.jmix.io/jmix/flow-ui/vc/components/datePicker.html). Use it if a popup field is acceptable; for an always-visible calendar use the Calendar add-on (jmix-fullcalendar) or a custom component", 2),
                UiComponentIssue.createChanged("embedded", "Use 'image' for images (https://docs.jmix.io/jmix/flow-ui/vc/html-components/image.html), html 'iframe' for embedded web pages, or 'htmlObject' for embedded objects", 2),
                UiComponentIssue.createChanged("fieldGroup", "Use 'formLayout'", 2),
                UiComponentIssue.createChanged("fileMultiUploadField", "Use 'upload' with 'MULTI_FILE_*' receiver type (https://docs.jmix.io/jmix/flow-ui/vc/components/upload.html)", 2),
                UiComponentIssue.createChanged("groupTable", "Add the commercial 'Grouping Data Grid' add-on (io.jmix.groupgrid, requires Enterprise subscription) and use 'groupDataGrid' (https://docs.jmix.io/jmix/groupdatagrid/index.html)", 3,
                        Requires.commercialAddon("Grouping Data Grid (io.jmix.groupgrid)")),
                UiComponentIssue.createChanged("link", "Use html 'anchor' (https://docs.jmix.io/jmix/flow-ui/vc/html.html)", 1),
                UiComponentIssue.createChanged("linkButton", "Use 'button' with themeNames=\"tertiary-inline\" for a borderless, link-style button (https://docs.jmix.io/jmix/flow-ui/vc/components/button.html#variants)", 1),
                UiComponentIssue.createChanged("lookupField", "Use 'comboBox' for plain values or 'entityComboBox' for entity references (https://docs.jmix.io/jmix/flow-ui/vc/components/comboBox.html)", 1),
                UiComponentIssue.createChanged("lookupPickerField", "Use 'entityComboBox' - it combines dropdown selection with picker actions (https://docs.jmix.io/jmix/flow-ui/vc/components/entityComboBox.html)", 1),
                UiComponentIssue.createAlternative("maskedField", "No built-in masked input. Use the 3rd-party Vaadin 'Input Mask' add-on: apply programmatically via new InputMask(\"...\").extend(field) (~2 lines), or wrap it in a small custom component for declarative XML use (https://demo.jmix.io/ui-samples/sample/input-mask-addon-advanced). For plain character restriction without formatting, use textField 'allowedCharPattern'", 2,
                        Requires.thirdParty("Vaadin 'Input Mask' add-on")),
                UiComponentIssue.createChanged("optionsGroup", "Use 'checkboxGroup' for multi-select or 'radioButtonGroup' for single-select", 1),
                UiComponentIssue.createChanged("optionsList", "Use 'listBox' for single selection or 'multiSelectListBox' for multiple selection", 2),
                UiComponentIssue.createChanged("pickerField", "Use 'entityPicker' (https://docs.jmix.io/jmix/flow-ui/vc/components/entityPicker.html)", 1),
                UiComponentIssue.createChanged("popupButton", "Use 'dropdownButton' (https://docs.jmix.io/jmix/flow-ui/vc/components/dropdownButton.html)", 1),
                UiComponentIssue.createWorkaround("popupView", "No direct equivalent. Open a 'dialog' with custom layout (https://docs.jmix.io/jmix/flow-ui/dialogs.html); 'sideDialog' and 'sidePanelLayout' are additional alternatives", 5),
                UiComponentIssue.createWorkaround("relatedEntities", "Use 'dropdownButton' with manual screen opening action", 5),
                UiComponentIssue.createAlternative("resizableTextArea", "Use 'textArea' and enable resizing via a CSS class applying the native 'resize' property (https://demo.jmix.io/ui-samples/sample/text-area-resizable). No built-in resizableDirection attribute or resize event", 1),
                UiComponentIssue.createChanged("searchPickerField", "Use 'entityComboBox' with nested 'itemsQuery' for query-driven lazy search (also supports picker actions); use 'comboBox' + 'itemsQuery' for non-entity values (https://docs.jmix.io/jmix/flow-ui/vc/components/entityComboBox.html)", 2),
                UiComponentIssue.createChanged("sideMenu", "Use 'listMenu'", 3),
                UiComponentIssue.createChanged("sourceCodeEditor", "Use 'codeEditor' (https://docs.jmix.io/jmix/flow-ui/vc/components/codeEditor.html). Check highlight mode support - the set of supported languages differs", 2),
                UiComponentIssue.createAlternative("slider", "Use HTML 'rangeInput' (an <input type='range'> supporting min/max/step/orientation, value type Double) for a basic range/slider input. It is not declaratively bindable to a data container - bind its value to the entity attribute manually in the controller. For a full-featured slider (tooltips, ticks, multiple handles), create a custom component with a 3rd-party JS library", 2),
                UiComponentIssue.createChanged("suggestionField", "Use 'comboBox'. It works with suggestion by default for non-entity values. See also its 'itemsQuery' parameter (https://demo.jmix.io/ui-samples/sample/combobox-items-query)", 2),
                UiComponentIssue.createChanged("suggestionPickerField", "Use 'entityComboBox' with nested 'itemsQuery' for search-as-you-type entity selection with picker actions (https://docs.jmix.io/jmix/flow-ui/vc/components/entityComboBox.html)", 2),
                UiComponentIssue.createAlternative("tokenList", "Use 'multiSelectComboBox' (chips), 'multiSelectComboBoxPicker' (with action buttons) or 'multiValuePicker'. Not a 1:1 token control - labels/wrapping differ, treat as an alternative (https://docs.jmix.io/jmix/flow-ui/vc/components/multiSelectComboBox.html)", 2),
                UiComponentIssue.createAlternative("tree", "No tree component in Jmix Flow UI. Use 'treeDataGrid' with a single column for hierarchical data (https://docs.jmix.io/jmix/flow-ui/vc/components/treeDataGrid.html)", 3),
                UiComponentIssue.createChanged("flowBox", "Use 'flexLayout' with flexWrap=\"WRAP\" (https://docs.jmix.io/jmix/flow-ui/vc/layouts/flexLayout.html)", 2),
                UiComponentIssue.createChanged("buttonsPanel", "Use 'hbox' with classNames=\"buttons-panel\" (https://docs.jmix.io/jmix/flow-ui/vc/layouts/hbox.html)", 1),
                UiComponentIssue.createChanged("cssLayout", "Use 'flexLayout' for a CSS-driven container or 'div' for a plain styled container; CSS styling via 'classNames'/'css' is supported by all layouts (https://docs.jmix.io/jmix/flow-ui/vc/layouts/flexLayout.html)", 0),
                UiComponentIssue.createChanged("frame", "Use 'fragment' (https://docs.jmix.io/jmix/flow-ui/fragments/fragments.html)", 5),
                UiComponentIssue.createChanged("grid", "Use 'gridLayout' for a 2D grid (https://docs.jmix.io/jmix/flow-ui/vc/layouts/gridLayout.html), or 'formLayout' for a form-like layout", 2),
                UiComponentIssue.createAlternative("htmlBox", "Use 'html' to render HTML markup as string/CDATA or from a file (https://docs.jmix.io/jmix/flow-ui/vc/components/html.html). It does not support HtmlBoxLayout component 'location' placeholders; re-lay-out nested components with standard layout containers", 3)
        ).collect(Collectors.toUnmodifiableMap(UiComponentIssue::getComponent, Function.identity()));
    }
}
