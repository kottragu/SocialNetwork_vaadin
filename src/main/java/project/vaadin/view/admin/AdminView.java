package project.vaadin.view.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import project.vaadin.entity.Role;
import project.vaadin.entity.User;
import project.vaadin.repo.UserRepo;
import java.util.Collection;
import java.util.Collections;
import java.util.WeakHashMap;


@Route("admin")
public class AdminView extends VerticalLayout {
    private UserRepo userRepo;
    private Grid<User> grid = new Grid<>(User.class);
    private TextField username = new TextField();
    private TextField password = new TextField();
    private Select<Role> roleSelect = new Select<>();
    private Editor<User> editor;


    @Autowired
    public AdminView(UserRepo repo) {
        userRepo = repo;
        grid.setItems(userRepo.findAll());

        Binder<User> binder = new Binder<>(User.class);
        editor = grid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        binder.bind(username, "username");
        grid.getColumnByKey("username").setEditorComponent(username);

        binder.bind(password, "password");
        grid.getColumnByKey("password").setEditorComponent(password);

        roleSelect.setItems(Role.values());
        binder.bind(roleSelect,"role");
        grid.getColumnByKey("role").setEditorComponent(roleSelect);

        Collection<Button> editButtons = Collections
                .newSetFromMap(new WeakHashMap<>());

        Grid.Column<User> editorColumn = grid.addComponentColumn(user -> {
            Button edit = new Button("edit");
            edit.addClassName("edit");
            edit.addClickListener( e -> {
                editor.editItem(user);
                username.focus();
            });
            edit.setEnabled(!editor.isOpen());
            editButtons.add(edit);
            return edit;
        });

        editor.addOpenListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));
        editor.addCloseListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));

        Button save = new Button("Save", e -> editor.save());
        save.addClassName("save");

        Button cancel = new Button("Cancel", e -> editor.cancel());
        cancel.addClassName("cancel");

        grid.getElement().addEventListener("keyup", event -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");

        Div buttons = new Div(save, cancel);
        editorColumn.setEditorComponent(buttons);

        editor.addSaveListener(e ->
            userRepo.updateUser(e.getItem().getId(), e.getItem().getUsername(), e.getItem().getPassword(), e.getItem().getRole()));

        add(grid);
    }

}
