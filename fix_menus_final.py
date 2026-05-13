import glob
import re

count = 0
pattern = r'<HBox[^>]*fx:id="menuOverlay".*?</HBox>\s*(?=(?:</StackPane>|</AnchorPane>|</children>|</HBox>|</VBox>))'

for filepath in glob.glob('src/main/resources/**/*.fxml', recursive=True):
    if 'OverlayMenu.fxml' in filepath or 'cnts_agent_home.fxml' in filepath:
        continue

    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    new_content, num_subs = re.subn(pattern, r'<fx:include source="OverlayMenu.fxml" fx:id="menuOverlay"/>\n', content, flags=re.DOTALL)
    
    if num_subs > 0:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        print(f"Updated {filepath}")
        count += 1

print(f"Total files updated: {count}")
