import os
import sys
import bpy

if bpy.context.space_data is None:
    cwd = os.path.dirname(os.path.abspath(__file__))
else:
    cwd = os.path.dirname(bpy.context.space_data.text.filepath)

# Get folder of script and add current working directory to path
sys.path.append(cwd)


def dedup_mesh(mesh_path: str, output_path: str, threshold: float):
    scene = bpy.context.scene

    # Select all mesh objects and delete them
    for o in scene.objects:
        if o.type == 'MESH':
            o.select = True
        else:
            o.select = False
    bpy.ops.object.delete()

    bpy.ops.import_mesh.stl(filepath=mesh_path)
    scene.objects.active = scene.objects[0]

    bpy.ops.object.mode_set(mode='EDIT')
    bpy.ops.mesh.remove_doubles(threshold=threshold)
    bpy.ops.object.mode_set(mode='OBJECT')

    bpy.ops.export_mesh.stl(filepath=output_path)


def main(argv):
    if len(argv) != 3:
        sys.exit(2)

    mesh_path = argv[0]
    output_path = argv[1]
    threshold = float(argv[2])

    dedup_mesh(mesh_path, output_path, threshold)


if __name__ == '__main__':
    argv = sys.argv
    argv = argv[argv.index("--") + 1:]  # get all args after "--"
    main(argv)
