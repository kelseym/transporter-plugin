import os
import json
import sys
import xnat
import requests
import re
import datetime


def generate_snap_item(subject_or_experiment_or_scan, base_type):
    item = {
        "id": subject_or_experiment_or_scan.id if hasattr(subject_or_experiment_or_scan, 'id') else "",
        "label": subject_or_experiment_or_scan.label if hasattr(subject_or_experiment_or_scan, 'label') else "",
        "xnat-type": "",
        "uri": subject_or_experiment_or_scan.uri,
        "children": []
    }

    # Collect Resources
    if 'session' in subject_or_experiment_or_scan.xpath.lower():
        item["xnat-type"] = "SESSION"
        for scan in subject_or_experiment_or_scan.scans.values():
            item["children"].append(generate_snap_item(scan, base_type))
    elif 'subject' in subject_or_experiment_or_scan.xpath.lower():
        item["xnat-type"] = "SUBJECT"
        for experiment in subject_or_experiment_or_scan.experiments.values():
            item["children"].append(generate_snap_item(experiment, base_type))
    elif 'scan' in subject_or_experiment_or_scan.xpath.lower():
        item["xnat-type"] = "SCAN"
        for resource in subject_or_experiment_or_scan.resources.values():
            item["children"].append(generate_snap_item(resource, base_type))
    elif 'resource' in subject_or_experiment_or_scan.xpath.lower():
        item["xnat-type"] = "RESOURCE"
        # Replace the trailing ID in these resources uri with its label
        item["uri"] = re.sub(r'(\d+)$', item['label'], item["uri"])
        if base_type.lower() == "resource":
            item["file-type"] = "DIRECTORY"
        elif base_type == "FILE":
            for index, file in enumerate(subject_or_experiment_or_scan.files.values()):
                item["children"].append({
                    "id": file.id if hasattr(file, 'id') else "",
                    "label": file.id if hasattr(file, 'label') else "",
                    "uri": file.uri,
                    "file-type": "FILE",
                    "xnat-type": "FILE",
                    "children": []
                })

    return item


if __name__ == "__main__":
    if len(sys.argv) < 2 | len(sys.argv) > 3:
        print("Usage: python datasnap.py PROJECT_ID BASE_TYPE ([FILE] | RESOURCE)")
        sys.exit(1)

    project_id = sys.argv[1]
    base_type = sys.argv[2] if len(sys.argv) == 3 else "FILE"

    # Get environment variables
    datasnap_name = os.environ.get('DATASNAP_NAME', 'DefaultName' + datetime.datetime.now().strftime('%m-%d-%H:%M:%S'))
    datasnap_desc = os.environ.get('DATASNAP_DESC', 'DefaultDescription')
    xnat_host = os.environ.get('XNAT_HOST', 'http://localhost')
    xnat_user = os.environ.get('XNAT_USER', 'admin')
    xnat_pass = os.environ.get('XNAT_PASS', 'admin')
    datasnap_endpoint = xnat_host + "/xapi/transporter/datasnap"

    with xnat.connect(xnat_host, user=xnat_user, password=xnat_pass) as session:
        project = session.projects[project_id]
        datasnap = {
            "label": datasnap_name,
            "description": datasnap_desc,
            "path-root-key": project_id,
            "base-type" : base_type,
            "content": [generate_snap_item(subject, base_type) for subject in project.subjects.values()]
        }

    print(json.dumps(datasnap, indent=4))

    # POST to the REST API endpoint
    print("Posting DataSnap to " + datasnap_endpoint)
    headers = {
        "Content-Type": "application/json"
    }
    params = {'resolve': True}

    response = requests.post(datasnap_endpoint, headers=headers, data=json.dumps(datasnap), auth=(xnat_user, xnat_pass),
                             params=params)

    # Check the response
    if response.status_code == 200:
        print(f"Data successfully posted! - {response.text}")
    else:
        print(f"Error occurred: {response.status_code} - {response.text}")
        exit(1)
