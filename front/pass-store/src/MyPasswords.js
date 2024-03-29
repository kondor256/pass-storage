import React, { useState,useEffect } from 'react';
import {Button, Collapse, Divider, Grid, IconButton, ListItem, ListItemIcon, Modal} from "@mui/material";

import List from '@mui/material/List';
import ListItemButton from "@mui/material/ListItemButton";
import ListItemText from "@mui/material/ListItemText";

import FolderIcon from '@mui/icons-material/Folder';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ControlPointIcon from '@mui/icons-material/ControlPoint';

import PasswordString from "./PasswordString";
import { FormPassword } from "./FormPassword";
import {green, red} from "@mui/material/colors";
import ShareIcon from "@mui/icons-material/Share";
import FolderListItem from "./FolderListItem";
import {FormFolder} from "./FormFolder";

const PASS_API = "/api/v1/pass";
const FOLDER_API = "/api/v1/folder";

function MyPasswords(props){
    const [passList, setPassList] = useState([]);
    const [editedPass, setEditedPass] = useState(null);
    const [folderTree, setFolderTree] = useState(null);
    const [editedFolder, setEditedFolder] = useState(null);
    const [activeFolder, setActiveFolder] = useState(null);
    const [activeTreeFolder, setActiveTreeFolder] = useState(null);
    const [openedFolders, setOpenedFolders] = useState(new Map());
    const [foldersChanged, setFoldersChanged] = useState(false);
    const [folderList, setFolderList] = useState([]);

    function getNewFolderTreeItem(folder){
        return {folder: folder, children: []};
    }

    const updatePassList = () => {
        fetch(PASS_API+"/list").then(resp => resp.json()).then(passList => {
            const passes = [];
            for (const pass of passList) {
                passes.push(pass);
            }
            setPassList(passes);
        });
    }
    const updateFolders = () => {
        fetch(FOLDER_API+"/list").then(resp => resp.json()).then(folderList => {
            const folderMap = new Map();
            const lFolderList = [];
            const folders = getNewFolderTreeItem({id: "0", name: "ALL", folderId: null});
            lFolderList.push(folders.folder);
            for (const folder of folderList){
                if (folderMap.has(folder.id)){
                    //Nothing
                } else {
                    folderMap.set(folder.id, getNewFolderTreeItem(folder));
                    lFolderList.push(folder);
                }
            }
            for (const folder of folderList){
                if (folderMap.has(folder.folderId)){
                    const parent = folderMap.get(folder.folderId);
                    parent.children.push(folderMap.get(folder.id));
                } else {
                    //add to first level
                    folders.children.push(folderMap.get(folder.id));
                }
                if (!openedFolders.has(folder.id)){
                    openedFolders.set(folder.id,false);
                }
            }
            setActiveFolder(folders.folder);
            setFolderTree(folders);
            setActiveTreeFolder(folders)
            setOpenedFolders(new Map(openedFolders));
            setFoldersChanged(v => !v);
            setFolderList(lFolderList);
            // setFolderTree((oldTree) => {console.log(oldTree); return folders;});

        });
    }

    useEffect(() => {
        updatePassList();
        updateFolders();
    },[])

    const handlePassFormClose = () => {
        setEditedPass(null);
    }
    const handleFolderFormClose = () => {
        setEditedFolder(null);
    }

    const createPass = () => {
        fetch(PASS_API+"/empty").then(resp => resp.json()).then(newPass => {
            if (activeFolder && activeFolder.id !== "0")
                newPass.folderId = activeFolder.id;
            setEditedPass(newPass);
        })
    }
    const postPass = (pass) => {
        fetch(PASS_API,{
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            body: JSON.stringify(pass)
        }).then(response => {
            if (response.ok){
                updatePassList();
            } else {
                console.log(response);
            }
        });
    }
    const deletePass = (pass) => {
        fetch(PASS_API,{
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            body: JSON.stringify(pass)
        }).then(response => {
            if (response.ok){
                updatePassList();
            } else {
                console.log(response);
            }
        });
    }

    const createFolder = (parentFolder) => {
        //console.log("Parent folder",parentFolder);
        fetch(FOLDER_API+"/empty").then(resp => resp.json()).then(newFolder => {
            if (parentFolder.id !== "0")
                newFolder.folderId = parentFolder.id;
            setEditedFolder(newFolder);
        })
    }
    const editFolder = (folder) => {
        setEditedFolder(folder);
    }
    const activateFolder = (folderTree) => {
        setActiveFolder(folderTree.folder);
        setActiveTreeFolder(folderTree);
    }
    const postFolder = (folder) => {
        fetch(FOLDER_API,{
            method: 'POST',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            body: JSON.stringify(folder)
        }).then(response => {
            if (response.ok){
                updateFolders();
            } else {
                console.log(response);
            }
        });
    }
    const deleteFolder = (folder) => {
        fetch(FOLDER_API,{
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json;charset=utf-8'
            },
            body: JSON.stringify(folder)
        }).then(response => {
            if (response.ok){
                updateFolders();
            } else {
                console.log(response);
            }
        });
    }

    const idInFolder = (folderId, folderTree) => {
        if (!folderId && folderTree.folder.id === "0") return true;
        if (folderTree.folder.id === folderId) return true;
        for (const childFolder of folderTree.children){
            if (idInFolder(folderId,childFolder)) return true;
        }
        return false;
    }
    const idInActiveFolder = (folderId) => {
        if (!activeTreeFolder) return true;
        return idInFolder(folderId,activeTreeFolder);
    }

    return([
        <Grid key={"gridMyPasswords"}
            container spacing={2}>
            <Grid item xs={4}>
                <List
                    sx={{ width: '100%', bgcolor: 'background.paper', borderRadius: 2 }}
                >
                    <FolderListItem
                        key={"folderTree"+foldersChanged}
                        level={0}
                        folder={folderTree}
                        openedFolders={openedFolders}
                        activeFolder={activeFolder}
                        handleNewFolder={createFolder}
                        handleActivateFolder={activateFolder}
                        handleEditFolder={editFolder}
                    />
                </List>
            </Grid>
            <Grid item xs={8}>
                <List
                    sx={{ width: '100%', bgcolor: 'background.paper', borderRadius: 2 }}
                >
                    {passList.map((pass) => !idInActiveFolder(pass.folderId)?null:
                       <PasswordString key={"pass"+pass.id}
                                       text={pass.description} pass={pass} onClickPass={(pass) => {setEditedPass(pass)}}/>
                    )}
                </List>
                <Divider sx={{margin: 1, border: 0}} />
                <Button variant="outlined" disabled={false} onClick={createPass}>Create password</Button>
            </Grid>
        </Grid>,
        <Modal
            key="PassForm"
            open={editedPass != null}
            onClose={handlePassFormClose}
        >
            <FormPassword pass={editedPass}
                          folderList={folderList}
                          handleClose={handlePassFormClose}
                          handleSavePass={postPass}
                          handleDeletePass={deletePass}/>
        </Modal>,
        <Modal
            key="FolderForm"
            open={editedFolder != null}
            onClose={handleFolderFormClose}
        >
            <FormFolder folder={editedFolder}
                        folderList={folderList}
                        handleClose={handleFolderFormClose}
                        handleSaveFolder={postFolder}
                        handleDeleteFolder={deleteFolder}
            />
        </Modal>
    ])
}

export default MyPasswords;