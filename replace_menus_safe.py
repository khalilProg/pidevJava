import os
import glob

count = 0
for filepath in glob.glob('src/main/resources/**/*.fxml', recursive=True):
    if 'OverlayMenu.fxml' in filepath:
        continue

    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    in_menu = False
    hbox_count = 0
    new_lines = []
    replaced = False

    for line in lines:
        if not in_menu:
            if 'fx:id="menuOverlay"' in line and '<HBox' in line:
                in_menu = True
                hbox_count = line.count('<HBox') - line.count('</HBox>')
                new_lines.append('        <fx:include source="OverlayMenu.fxml" fx:id="menuOverlay"/>\n')
                replaced = True
                if hbox_count == 0:
                    in_menu = False
            else:
                new_lines.append(line)
        else:
            hbox_count += line.count('<HBox')
            hbox_count -= line.count('</HBox>')
            if hbox_count <= 0:
                in_menu = False

    if replaced:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
        print(f"Updated {filepath}")
        count += 1

print(f"Total files updated: {count}")
