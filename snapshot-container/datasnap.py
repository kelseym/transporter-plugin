import os
import json
import sys
import xnat
import requests


def generate_snap_item(subject_or_experiment_or_scan):
    item = {
        "id": subject_or_experiment_or_scan.id if hasattr(subject_or_experiment_or_scan, 'id') else "",
        "label": subject_or_experiment_or_scan.id if hasattr(subject_or_experiment_or_scan, 'label') else "",
        "xnat-type"
        "uri": subject_or_experiment_or_scan.uri,
        "children": []
    }

    if hasattr(subject_or_experiment_or_scan, 'scans'):  # This is an experiment
        item["file-type"] = "DIRECTORY"
        for scan in subject_or_experiment_or_scan.scans.values():
            item["children"].append(generate_snap_item(scan))
    elif hasattr(subject_or_experiment_or_scan, 'experiments'):  # This is a subject
        item["file-type"] = "DIRECTORY"
        for experiment in subject_or_experiment_or_scan.experiments.values():
            item["children"].append(generate_snap_item(experiment))
    elif hasattr(subject_or_experiment_or_scan, 'files'):  # This is a scan
        item["file-type"] = "DIRECTORY"
        for file in subject_or_experiment_or_scan.files.values():
            item["children"].append({
                "id": file.id if hasattr(file, 'id') else "",
                "label": file.id if hasattr(file, 'label') else "",
                "uri": file.uri,
                "file-type": "FILE",
                "children": []
            })

    return item


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python datasnap.py PROJECT_ID")
        sys.exit(1)

    project_id = sys.argv[1]

    # Get environment variables
    datasnap_name = os.environ.get('DATASNAP_NAME', 'DefaultName')
    datasnap_desc = os.environ.get('DATASNAP_DESC', 'DefaultDescription')
    xnat_host = os.environ.get('XNAT_HOST')
    xnat_user = os.environ.get('XNAT_USER')
    xnat_pass = os.environ.get('XNAT_PASS')
    datasnap_endpoint = xnat_host + "/xapi/transporter/datasnap"
    datasnap = ""

    with xnat.connect(xnat_host, user=xnat_user, password=xnat_pass) as session:
        project = session.projects[project_id]
        datasnap = {
            "label": datasnap_name,
            "description": datasnap_desc,
            "content": [generate_snap_item(subject) for subject in project.subjects.values()]
        }

    print(json.dumps(datasnap, indent=4))

    # POST to the REST API endpoint
    print("Posting DataSnap to" + datasnap_endpoint)
    headers = {
        "Content-Type": "application/json"
    }
    response = requests.post(datasnap_endpoint, headers=headers, data=json.dumps(datasnap), auth=(xnat_user, xnat_pass))

    # Check the response
    if response.status_code == 200:
        print("Data successfully posted!")
    else:
        print(f"Error occurred: {response.status_code} - {response.text}")
