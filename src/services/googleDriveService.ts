/**
 * Google Drive Sync Service for PequeCare
 * Integrates directly with Google Drive API v3 using user OAuth access token
 */

export interface DriveFileInfo {
  id: string;
  name: string;
  modifiedTime: string;
  size?: string;
}

export const getBackupFileName = (childName: string) => {
  const safeName = (childName || 'Menor').trim().replace(/[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ_-]/g, '_');
  return `PequeCare_Respaldo_Salud_${safeName}.json`;
};

/**
 * Searches for an existing backup file in the user's Google Drive
 */
export const findDriveBackup = async (
  accessToken: string,
  childName: string
): Promise<DriveFileInfo | null> => {
  const fileName = getBackupFileName(childName);
  const query = encodeURIComponent(`name = '${fileName}' and trashed = false`);
  const url = `https://www.googleapis.com/drive/v3/files?q=${query}&fields=files(id,name,modifiedTime,size)&pageSize=1`;

  const response = await fetch(url, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Error al buscar en Google Drive: ${response.status} - ${errorText}`);
  }

  const data = await response.json();
  if (data.files && data.files.length > 0) {
    return data.files[0] as DriveFileInfo;
  }
  return null;
};

/**
 * Uploads or updates the backup JSON file in Google Drive
 */
export const syncDataToDrive = async (
  accessToken: string,
  childName: string,
  appData: any
): Promise<DriveFileInfo> => {
  const fileName = getBackupFileName(childName);
  const existingFile = await findDriveBackup(accessToken, childName);

  const fileContent = JSON.stringify(appData, null, 2);

  if (existingFile) {
    // Update existing file content
    const updateUrl = `https://www.googleapis.com/upload/drive/v3/files/${existingFile.id}?uploadType=media`;
    const res = await fetch(updateUrl, {
      method: 'PATCH',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
      body: fileContent,
    });

    if (!res.ok) {
      throw new Error(`Error al actualizar respaldo en Drive: ${res.statusText}`);
    }

    return {
      id: existingFile.id,
      name: fileName,
      modifiedTime: new Date().toISOString(),
    };
  } else {
    // Create new file in Google Drive using multipart upload
    const metadata = {
      name: fileName,
      mimeType: 'application/json',
      description: `Copia de seguridad médica y curvas de crecimiento de ${childName} creada por PequeCare`,
    };

    const boundary = '-------314159265358979323846';
    const delimiter = `\r\n--${boundary}\r\n`;
    const closeDelimiter = `\r\n--${boundary}--`;

    const multipartRequestBody =
      delimiter +
      'Content-Type: application/json; charset=UTF-8\r\n\r\n' +
      JSON.stringify(metadata) +
      delimiter +
      'Content-Type: application/json\r\n\r\n' +
      fileContent +
      closeDelimiter;

    const createUrl = 'https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart';
    const res = await fetch(createUrl, {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': `multipart/related; boundary=${boundary}`,
      },
      body: multipartRequestBody,
    });

    if (!res.ok) {
      const err = await res.text();
      throw new Error(`Error al crear respaldo en Drive: ${res.status} - ${err}`);
    }

    const created = await res.json();
    return {
      id: created.id,
      name: created.name,
      modifiedTime: new Date().toISOString(),
    };
  }
};

/**
 * Downloads backup JSON file data from Google Drive
 */
export const loadDataFromDrive = async (
  accessToken: string,
  fileId: string
): Promise<any> => {
  const downloadUrl = `https://www.googleapis.com/drive/v3/files/${fileId}?alt=media`;
  const res = await fetch(downloadUrl, {
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });

  if (!res.ok) {
    throw new Error(`Error al descargar datos de Google Drive: ${res.statusText}`);
  }

  return await res.json();
};
