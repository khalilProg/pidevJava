import os
import glob
import re

count = 0
for filepath in glob.glob('/home/khalil/bloodlink/src/main/resources/**/*.fxml', recursive=True):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # We don't want to replace inside OverlayMenu.fxml itself
    if 'OverlayMenu.fxml' in filepath:
        continue

    # Search for an HBox with fx:id="menuOverlay"
    if 'fx:id="menuOverlay"' in content:
        new_content, num_subs = re.subn(r'<HBox[^>]*fx:id="menuOverlay"[^>]*>.*?</HBox>', '<fx:include source="OverlayMenu.fxml" fx:id="menuOverlay"/>', content, flags=re.DOTALL)
        if num_subs > 0:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"Updated {filepath}")
            count += 1

print(f"Total files updated: {count}")
