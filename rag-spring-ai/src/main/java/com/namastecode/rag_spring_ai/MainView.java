package com.namastecode.rag_spring_ai;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.capitalize;

@Route(value = "")
@PageTitle("Chatbot")
public class MainView extends VerticalLayout {

    private final transient VectorStore vectorStore;
    private final transient JdbcTemplate jdbcTemplate;
    private final MultiSelectComboBox<String> uploadedFiles;
    private final ChatService chatService;

    private final VerticalLayout chatContainer;

    public MainView(VectorStore vectorStore,
                    JdbcTemplate jdbcTemplate,
                    ChatService chatService) {
        this.vectorStore = vectorStore;
        this.jdbcTemplate = jdbcTemplate;
        this.chatService = chatService;
        chatContainer = createChatContainer();
        setSizeFull();
        TextField messageField = createMessageField();

        Button askButton = createSendButton(chatService, messageField);
        Button clearHistory = createClearHistoryButton();

        HorizontalLayout messageBar = new HorizontalLayout(messageField, askButton, clearHistory);
        messageBar.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        messageBar.setWidthFull();
        messageBar.setFlexGrow(1, messageField);

        Scroller scroller = new Scroller(chatContainer);
        scroller.setWidthFull();

        VerticalLayout chatBox = new VerticalLayout();
        chatBox.setMaxHeight(80, Unit.PERCENTAGE);
        chatBox.add(scroller, messageBar);
        chatBox.setFlexGrow(1, scroller);

        uploadedFiles = new MultiSelectComboBox<>("Files to include");
        uploadedFiles.setWidthFull();

        Upload upload = createUploadComponent();

        HorizontalLayout filesLayout = new HorizontalLayout(upload, uploadedFiles);
        filesLayout.setWidthFull();
        filesLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        filesLayout.setFlexGrow(1, uploadedFiles);
        add(filesLayout, new Hr(), chatBox);
        setFlexGrow(1, chatBox);
        setWidthFull();
    }

    private Button createClearHistoryButton() {
        Button clearHistory = new Button(VaadinIcon.TRASH.create(), handleSessionClear());
        clearHistory.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return clearHistory;
    }

    private Button createSendButton(ChatService chatService, TextField messageField) {
        Button askButton = new Button(VaadinIcon.PAPERPLANE.create(), handleNewMessageRequest(chatService, messageField, chatContainer));
        askButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        askButton.addClickShortcut(Key.ENTER);
        return askButton;
    }

    private Upload createUploadComponent() {
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes(".txt", ".md", ".pdf");
        upload.setMaxFileSize(1024 * 1024 * 10);
        upload.addSucceededListener(handleUpload(buffer));
        upload.setWidthFull();

        List<String> savedResources = jdbcTemplate.queryForList("SELECT distinct metadata->>'source' FROM vector_store", String.class);
        uploadedFiles.setItems(savedResources);

        return upload;
    }

    private ComponentEventListener<ClickEvent<Button>> handleSessionClear() {
        return e -> {
            chatService.clearChatMemory(UI.getCurrent().getSession().getSession().getId());
            chatContainer.removeAll();
        };
    }

    private ComponentEventListener<SucceededEvent> handleUpload(MultiFileMemoryBuffer buffer) {
        return event -> {
            String fileName = event.getFileName();
            TikaDocumentReader tikaReader = new TikaDocumentReader(new InputStreamResource(buffer.getInputStream(fileName)));
            TextSplitter textSplitter = new TokenTextSplitter();
            List<Document> parsedDocuments = textSplitter.apply(tikaReader.get());
            parsedDocuments.forEach(document -> document.getMetadata().put("source", fileName));
            vectorStore.accept(
                    parsedDocuments);
            uploadedFiles.getListDataView().addItem(fileName);
        };
    }

    private ComponentEventListener<ClickEvent<Button>> handleNewMessageRequest(ChatService service, TextField messageField, VerticalLayout chatContainer) {
        return _ -> {
            if (StringUtils.isBlank(messageField.getValue())) {
                Notification.show("Please enter a question");
            } else {
                chatContainer.add(getMessageBlock(new UserMessage(messageField.getValue())));
                AssistantMessage answer = service.ask(messageField.getValue(),
                                UI.getCurrent().getSession().getSession().getId(), uploadedFiles.getValue())
                        .getResult().getOutput();
                chatContainer.add(getMessageBlock(answer));
            }
        };
    }

    private TextField createMessageField() {
        TextField questionField = new TextField("Ask your question");
        questionField.setWidthFull();
        return questionField;
    }

    private VerticalLayout createChatContainer() {
        VerticalLayout container = new VerticalLayout();
        container.setWidthFull();
        container.setSpacing(false);
        container.setPadding(false);
        String sessionId = UI.getCurrent().getSession().getSession().getId();
        chatService.getChatHistory(sessionId)
                .forEach(message -> container.add(getMessageBlock(message)));
        return container;
    }

    private VerticalLayout getMessageBlock(Message message) {
        VerticalLayout messageContainer = new VerticalLayout();
        messageContainer.setPadding(false);
        messageContainer.setSpacing(false);
        messageContainer.addClassName(LumoUtility.FontSize.SMALL);

        Span messageFrom = new Span(capitalize(message.getMessageType().getValue()));
        messageFrom.addClassNames(LumoUtility.Padding.NONE,
                LumoUtility.Margin.NONE,
                LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.Border.BOTTOM,
                LumoUtility.Margin.XSMALL);

        messageContainer.add(messageFrom);

        Paragraph paragraph = new Paragraph(message.getContent());
        paragraph.addClassNames(LumoUtility.Margin.NONE, LumoUtility.Padding.NONE);
        messageContainer.add(paragraph);
        return messageContainer;
    }


}
