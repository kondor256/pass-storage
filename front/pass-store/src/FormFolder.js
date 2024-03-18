import React, { useState,useEffect } from 'react';
import Typography from "@mui/material/Typography";
import {
    Autocomplete,
    Button,
    Divider,
    FormControl,
    IconButton,
    Input,
    InputAdornment,
    InputLabel,
    TextField
} from "@mui/material";
import Box from "@mui/material/Box";

import DeleteForeverIcon from '@mui/icons-material/DeleteForever';

import {styleModal} from "./StyleModal";
import {red} from "@mui/material/colors";

export const FormFolder = React.forwardRef((props, ref) => {
    const [folder, setFolder] = useState({...props.folder});
    const [folderList, setFolderList] = useState([]);
    const [parentFolder, setParentFolder] = useState(null);
    const newFolder = props.folder.id == null;

    const editFolderOK = () => {
        if (parentFolder && parentFolder.id === "0")
            folder.folderId = null;
        else
            folder.folderId = parentFolder.id;

        Object.assign(props.folder, folder);
        props.handleSaveFolder(folder);
        props.handleClose();
    }
    const deleteFolder = () => {
        props.handleDeleteFolder(folder);
        props.handleClose();
    }

    useEffect(()=>{
        const fList = [];
        for (const pFolder of props.folderList) {
            if (pFolder.id === folder.id) continue;
            fList.push(pFolder);
            if (folder.folderId === pFolder.id || (folder.folderId == null && pFolder.id === "0"))
                setParentFolder(pFolder);
        }
        setFolderList(fList);
    },[])

    return(
        <Box sx={styleModal}>
            <Typography variant="h4">{newFolder?"New folder":"Folder"}</Typography>
            <Autocomplete
                disablePortal
                value={parentFolder}
                renderInput={(params) => <TextField {...params}  label="Parent folder" variant="standard" fullWidth={true} />}
                onChange={(e,v) => {
                    setParentFolder(v)}}
                options={folderList}
                getOptionLabel={folder => folder.name}
                isOptionEqualToValue={(o,v) => (o.id === v.id)}/>
            <Divider sx={{margin: 1, border: 0}} />
            <TextField id="valName" label="Name" variant="standard" fullWidth={true}
                       defaultValue={folder.name}
                       onChange={(e) => {folder.name = e.target.value}} />
            <Box sx={{ m: 1 }}>
                <Button variant="outlined" onClick={editFolderOK}>OK</Button>
                <Button variant="outlined" onClick={props.handleClose}>Cancel</Button>
                {newFolder?null:<Button variant="outlined" startIcon={<DeleteForeverIcon sx={{color: red[700]}}/>} onClick={deleteFolder}>DELETE</Button>}
            </Box>
        </Box>
    )
})