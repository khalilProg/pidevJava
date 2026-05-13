import glob

# The exact new menu we want to inject
NEW_MENU = """    <HBox fx:id="menuOverlay" alignment="CENTER_RIGHT" managed="false" visible="false">
        <Region onMouseClicked="#handleMenuClose" styleClass="menu-backdrop" HBox.hgrow="ALWAYS" />
        <VBox fx:id="menuPanel" alignment="TOP_LEFT" prefWidth="380.0" spacing="15.0" styleClass="menu-panel">
            <padding>
                <Insets bottom="40.0" left="40.0" right="40.0" top="30.0" />
            </padding>
            <children>
                <HBox alignment="CENTER_RIGHT">
                    <Button mnemonicParsing="false" onAction="#handleMenuToggle" styleClass="submit-button" text="FERMER" />
                </HBox>
                <Region prefHeight="20.0" />
                <Button mnemonicParsing="false" onAction="#goToAccueil" styleClass="menu-link" text="ACCUEIL" />
                <Button mnemonicParsing="false" onAction="#goToCampagnes" styleClass="menu-link" text="CAMPAGNES" />
                <Button mnemonicParsing="false" onAction="#goToHistorique" styleClass="menu-link" text="HISTORIQUE RDV" />
                <Button mnemonicParsing="false" onAction="#goToCommandes" styleClass="menu-link" text="COMMANDES" />
                <Button mnemonicParsing="false" onAction="#goToDons" styleClass="menu-link" text="MES DONS" />
                <Button mnemonicParsing="false" onAction="#goToDossierMed" styleClass="menu-link" text="DOSSIER MEDICAL" />
                <Button mnemonicParsing="false" onAction="#goToProfile" styleClass="menu-link" text="MON PROFIL" />
                <Region VBox.vgrow="ALWAYS" />
                <VBox spacing="15.0">
                    <Label fx:id="sessionEmailLabel" styleClass="menu-session" text="Session: utilisateur@email.com" />
                    <Button maxWidth="1.7976931348623157E308" mnemonicParsing="false" onAction="#handleLogout" styleClass="submit-button" text="Deconnexion" />
                </VBox>
            </children>
        </VBox>
    </HBox>"""

def replace_menu_in_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find where <HBox fx:id="menuOverlay" starts
    start_idx = content.find('<HBox fx:id="menuOverlay"')
    if start_idx == -1:
        # Also try single quotes or different spacing if needed
        start_idx = content.find("fx:id=\"menuOverlay\"")
        if start_idx != -1:
             start_idx = content.rfind("<HBox", 0, start_idx)

    if start_idx == -1:
        return

    # Find the closing tag using a stack
    end_idx = -1
    stack = []
    i = start_idx
    while i < len(content):
        if content[i:].startswith('<HBox'):
            stack.append('HBox')
            i += 5
        elif content[i:].startswith('</HBox>'):
            stack.pop()
            if len(stack) == 0:
                end_idx = i + 7
                break
            i += 7
        else:
            i += 1

    if end_idx != -1:
        # Check indentation to match
        line_start = content.rfind('\n', 0, start_idx)
        indent = ""
        if line_start != -1:
            indent = content[line_start+1:start_idx]
            if not indent.isspace():
                indent = ""
        
        indented_menu = NEW_MENU.replace('\n', '\n' + indent)
        new_content = content[:start_idx] + indented_menu + content[end_idx:]
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated menu in {filepath}")

for filepath in glob.glob('/home/khalil/bloodlink/src/main/resources/**/*.fxml', recursive=True):
    replace_menu_in_file(filepath)

