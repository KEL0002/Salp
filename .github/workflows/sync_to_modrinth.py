import requests
import os
import pathlib

modrinth_pat = os.environ["MODRINTH_PAT"]
modrinth_id = 'salp'
github_id = 'KEL0002/Salp'

def sync_readme():
    def get_readme() -> str:
        return pathlib.Path('README.md').read_text()

    def upload_readme(text):
        json = {
            'body': text
        }
        headers = {
            "Authorization": modrinth_pat,
            "Content-Type": "application/json",
            "User-Agent": github_id
        }
        response = requests.patch(f"https://api.modrinth.com/v2/project/{modrinth_id}", headers=headers, json=json)
        return response

    try:
        print("Getting github readme...")
        readme_text = get_readme()

        print("Got readme, uploading to Modrinth...")
        modrinth_response = upload_readme(readme_text)

        if modrinth_response.status_code != 204:
            print(f"Error uploading to Modrinth (Status Code {modrinth_response.status_code})")
            print(modrinth_response.content)
        else:
            print("Successfully uploaded!")

    except Exception as e:
        print(f"An error happened somewhere: {e}")

sync_readme()