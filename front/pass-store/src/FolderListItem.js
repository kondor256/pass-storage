import React, { useState, forwardRef, useEffect } from 'react';
import {Collapse, IconButton, ListItem, ListItemIcon} from "@mui/material";
import ControlPointIcon from "@mui/icons-material/ControlPoint";
import {green} from "@mui/material/colors";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import ListItemButton from "@mui/material/ListItemButton";
import FolderIcon from "@mui/icons-material/Folder";
import ListItemText from "@mui/material/ListItemText";

// export const FolderListItem = forwardRef((props, ref) => {
function FolderListItem(props){
    const [folder, setFolder] = useState(props.folder);
    if (!folder) return(
        <ListItem key="Loading">Loading...</ListItem>
    );

    const [open, setOpen] = useState(props.openedFolders.get(folder.folder.id));
    const [level, setLevel] = useState(props.level);

    useEffect(() => {
    },[])
    const handleClick = () => {
        props.openedFolders.set(folder.folder.id,!open);
        setOpen(!open);
    };
    const stringOnClick = (folderTree) => {
        if (props.activeFolder.id === folderTree.folder.id && folderTree.folder.id !== "0" && !folderTree.folder.shared)
            props.handleEditFolder(folderTree.folder);
        else
            props.handleActivateFolder(folderTree);
    }

    const addButton = folder.folder.shared?null:
        <IconButton key="buttonShare" edge="end" aria-label="share" sx={{marginRight: 0}}
                    onClick={props.handleNewFolder.bind(null,folder.folder)}>
        <ControlPointIcon sx={{color: green[700]}}/>
    </IconButton>;


    return([
        <ListItem key={folder.folder.id+folder.children.length} sx={{paddingTop: 0, paddingBottom: 0}}
                  secondaryAction={[
                      addButton,
                      folder.children.length>0?(open ? <ExpandLessIcon key="buttonExpand"
                                             onClick={handleClick}/> : <ExpandMoreIcon key="buttonExpand" onClick={handleClick}/>):null
                  ]}
        >
            <ListItemButton dense sx={{ pl: level*2 }}
                            selected={props.activeFolder.id === folder.folder.id}
            >
                <ListItemIcon>
                    <FolderIcon />
                </ListItemIcon>
                <ListItemText primary={folder.folder.name}
                              onClick={stringOnClick.bind(null,folder)}/>
            </ListItemButton>
        </ListItem>,
        <Collapse key={folder.folder.id+folder.children.length+"children"} in={open} timeout="auto" unmountOnExit>
            {folder.children.length === 0?null:folder.children.map( folder => <FolderListItem
                key={folder.folder.id+folder.children.length}
                level={level+1}
                folder={folder}
                openedFolders={props.openedFolders}
                activeFolder={props.activeFolder}
                handleNewFolder={props.handleNewFolder}
                handleActivateFolder={props.handleActivateFolder}
                handleEditFolder={props.handleEditFolder}
            />)}
        </Collapse>
    ]);
}

export default FolderListItem;